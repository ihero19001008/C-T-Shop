package com.app.ecommerce.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.android.volley.toolbox.JsonObjectRequest;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.app.ecommerce.models.Cart;
import com.app.ecommerce.models.Detail;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.ecommerce.Config;
import com.app.ecommerce.R;
import com.app.ecommerce.utilities.DBHelper;
import com.app.ecommerce.utilities.SharedPref;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import vn.momo.momo_partner.AppMoMoLib;
import vn.momo.momo_partner.ClientHttpAsyncTask;
import vn.momo.momo_partner.MoMoParameterNamePayment;

import static com.app.ecommerce.utilities.Constant.GET_SHIPPING;
import static com.app.ecommerce.utilities.Constant.POST_ORDER;
import static com.app.ecommerce.utilities.Constant.POST_ORDER_ITEM;

public class ActivityCheckout extends AppCompatActivity implements ClientHttpAsyncTask.RequestToServerListener {
    private Integer order_id;
    RequestQueue requestQueue;
    Button btn_submit_order, btn_submit_momo;
    EditText edt_name, edt_email, edt_phone, edt_address, edt_shipping, edt_order_list, edt_order_total, edt_comment,edt_total_momo;
    String str_name, str_email, str_phone, str_address, str_shipping, str_order_list, str_order_total, str_comment;
    String data_order_list = "";
    double str_tax;
    String str_currency_code;
    ProgressDialog progressDialog;
    DBHelper dbhelper;
    ArrayList<ArrayList<Object>> data;
    ArrayList<ArrayList<Object>> data2;
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    View view;
    List<Cart> list_cart = new ArrayList<>();
    List<Detail> list_detail = new ArrayList<>();
    private String rand = getRandomString(9);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = dateFormat.format(Calendar.getInstance().getTime().getTime());
    SharedPref sharedPref;
    private Spinner spinner;
    private ArrayList<String> arrayList;
    private JSONArray result;
    String Result;
    int environment = 1;//developer default
    private String merchantName = "Cửa hàng gia dụng C&T";
    private String merchantCode = "CGV19072017";
    private String merchantNameLabel = "Nhà cung cấp";
    private String description = "Thanh toán dịch vụ ABC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        view = findViewById(android.R.id.content);
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);
        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        sharedPref = new SharedPref(this);

        setupToolbar();
        getSpinnerData();
        getTaxCurrency();

        dbhelper = new DBHelper(this);
        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(ActivityCheckout.this);
        progressDialog = new ProgressDialog(ActivityCheckout.this);

        btn_submit_order = findViewById(R.id.btn_submit_order);
        btn_submit_momo = findViewById(R.id.btn_momo);

        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_phone = findViewById(R.id.edt_phone);
        edt_address = findViewById(R.id.edt_address);
        edt_shipping = findViewById(R.id.edt_shipping);
        edt_order_list = findViewById(R.id.edt_order_list);
        edt_order_total = findViewById(R.id.edt_order_total);
        edt_comment = findViewById(R.id.edt_comment);
        edt_total_momo = findViewById(R.id.edt_total_momo);

        edt_order_list.setEnabled(false);

        getDataFromDatabase();
        submitOrder();

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_checkout);
        }
    }

    private void getSpinnerData() {

        arrayList = new ArrayList<String>();
        spinner = findViewById(R.id.spinner);

        StringRequest stringRequest = new StringRequest(GET_SHIPPING, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    result = jsonObject.getJSONArray("result");
                    getShipping(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        edt_shipping.setText(setShipping(i));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void getShipping(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                arrayList.add(json.getString("shipping_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ActivityCheckout.this, R.layout.spinner_item, arrayList);
        myAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
    }

    private String setShipping(int position) {
        String name = "";
        try {
            JSONObject json = result.getJSONObject(position);
            name = json.getString("shipping_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void submitOrder() {
        btn_submit_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValueFromEditText();
            }
        });
        btn_submit_momo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValueFromEditTextMoMo();
            }
        });
    }

    public void getValueFromEditText() {

        str_name = edt_name.getText().toString();
        str_email = edt_email.getText().toString();
        str_phone = edt_phone.getText().toString();
        str_address = edt_address.getText().toString();
        str_shipping = edt_shipping.getText().toString();
        str_order_list = edt_order_list.getText().toString();
        str_order_total = edt_order_total.getText().toString();
        str_comment = edt_comment.getText().toString();

        if (str_name.equalsIgnoreCase("") ||
                str_email.equalsIgnoreCase("") ||
                str_phone.equalsIgnoreCase("") ||
                str_address.equalsIgnoreCase("") ||
                str_shipping.equalsIgnoreCase("") ||
                str_order_list.equalsIgnoreCase("")) {
            Snackbar.make(view, R.string.checkout_fill_form, Snackbar.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.checkout_dialog_title);
            builder.setMessage(R.string.checkout_dialog_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //requestAction();
                    requestAction2();

                }
            });
            builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), null);
            builder.setCancelable(false);
            builder.show();
        }
    }
    public void getValueFromEditTextMoMo() {

        str_name = edt_name.getText().toString();
        str_email = edt_email.getText().toString();
        str_phone = edt_phone.getText().toString();
        str_address = edt_address.getText().toString();
        str_shipping = edt_shipping.getText().toString();
        str_order_list = edt_order_list.getText().toString();
        str_order_total = edt_order_total.getText().toString();
        str_comment = edt_comment.getText().toString();

        if (str_name.equalsIgnoreCase("") ||
                str_email.equalsIgnoreCase("") ||
                str_phone.equalsIgnoreCase("") ||
                str_address.equalsIgnoreCase("") ||
                str_shipping.equalsIgnoreCase("") ||
                str_order_list.equalsIgnoreCase("")) {
            Snackbar.make(view, R.string.checkout_fill_form, Snackbar.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.checkout_dialog_title);
            builder.setMessage(R.string.checkout_dialog_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    requestPayment();

                }
            });
            builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), null);
            builder.setCancelable(false);
            builder.show();
        }
    }

    public void requestAction2() {
        progressDialog.setTitle(getString(R.string.checkout_submit_title));
        progressDialog.setMessage(getString(R.string.checkout_submit_msg));
        progressDialog.show();
        if (OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId() == null) {
            AndroidNetworking.post(POST_ORDER)
                    .addBodyParameter("code", rand)
                    .addBodyParameter("name", str_name)
                    .addBodyParameter("email", str_email)
                    .addBodyParameter("phone", str_phone)
                    .addBodyParameter("address", str_address)
                    .addBodyParameter("shipping", str_shipping)
                    .addBodyParameter("order_list", str_order_list)
                    .addBodyParameter("order_total", str_order_total)
                    .addBodyParameter("comment", str_comment)
                    .addBodyParameter("player_id", "0")
                    .addBodyParameter("date", date)
                    .addBodyParameter("server_url", Config.ADMIN_PANEL_URL)
                    .setTag("test")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String id_order = response.getString("id");
                                Log.d("id", id_order);
                                for (int i = 0; i < list_cart.size(); i++) {
                                    requestDetail(i, id_order);
                                }

                            } catch (JSONException e) {
                                progressDialog.dismiss();
                                Toast.makeText(ActivityCheckout.this, "Dat hang that bai", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            progressDialog.dismiss();
                            Toast.makeText(ActivityCheckout.this, "Dat hang that bai", Toast.LENGTH_SHORT).show();
                            Toast.makeText(ActivityCheckout.this, error.toString(), Toast.LENGTH_SHORT).show();
                            // handle error
                        }
                    });
        } else {
            AndroidNetworking.post(POST_ORDER)
                    .addBodyParameter("code", rand)
                    .addBodyParameter("name", str_name)
                    .addBodyParameter("email", str_email)
                    .addBodyParameter("phone", str_phone)
                    .addBodyParameter("address", str_address)
                    .addBodyParameter("shipping", str_shipping)
                    .addBodyParameter("order_list", str_order_list)
                    .addBodyParameter("order_total", str_order_total)
                    .addBodyParameter("comment", str_comment)
                    .addBodyParameter("player_id", OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId())
                    .addBodyParameter("date", date)
                    .addBodyParameter("server_url", Config.ADMIN_PANEL_URL)
                    .setTag("test")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //     Log.d("response",response.toString());
                            try {
                                String id_order = response.getString("id");
                                Log.d("id", id_order);
                                for (int i = 0; i < list_cart.size(); i++) {
                                    requestDetail(i, id_order);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                                Toast.makeText(ActivityCheckout.this, "Dat hang that bai", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.d("response", error.toString());
                            progressDialog.dismiss();
                            Toast.makeText(ActivityCheckout.this, "Dat hang that bai", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void requestDetail(int i, String order_id) {
        String product_name = list_cart.get(i).getMenuName();
        String product_id = list_cart.get(i).getMenuId();
        String quantity = list_cart.get(i).getMenuQuantity();
        String price = list_cart.get(i).getMenuPrice();
        Long uPrice = Long.parseLong(price) / Long.parseLong(quantity);
        String unitPrice = uPrice.toString();
        AndroidNetworking.post(POST_ORDER_ITEM)
                .addBodyParameter("code", rand)
                .addBodyParameter("order_id", order_id)
                .addBodyParameter("product_id", product_id)
                .addBodyParameter("quantity", quantity)
                .addBodyParameter("price", unitPrice)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                dialogSuccessOrder();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("response", error.toString());
                        progressDialog.dismiss();
                        Toast.makeText(ActivityCheckout.this, "Dat hang that bai", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void getTaxCurrency() {
        Intent intent = getIntent();
        str_tax = intent.getDoubleExtra("tax", 0);
        str_currency_code = intent.getStringExtra("currency_code");
    }

    public void getDataFromDatabase() {
        data = dbhelper.getAllData();
        double Order_price = 0;
        double Total_price = 0;
        double tax = 0;

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            String Menu_name = row.get(1).toString();
            String Quantity = row.get(2).toString();
            String ID = row.get(0).toString();
            String Price = row.get(3).toString();
            Cart cart = new Cart(ID, Menu_name, Quantity, Price);
            list_cart.add(cart);
            double Sub_total_price = Double.parseDouble(row.get(3).toString());

            String _Sub_total_price = String.format(Locale.getDefault(), "%1$,.0f", Sub_total_price);

            Order_price += Sub_total_price;

            if (Config.ENABLE_DECIMAL_ROUNDING) {
                data_order_list += (Quantity + " " + Menu_name + " " + _Sub_total_price + " " + str_currency_code + ",\n");
            } else {
                data_order_list += (Quantity + " " + Menu_name + " " + Sub_total_price + " " + str_currency_code + ",\n");
            }
        }

        if (data_order_list.equalsIgnoreCase("")) {
            data_order_list += getString(R.string.no_order_menu);
        }

        tax = Order_price * (str_tax / 100);
        Total_price = Order_price + tax;

        String price_tax = String.format(Locale.getDefault(), "%1$,.0f", str_tax);
        String _Order_price = String.format(Locale.getDefault(), "%1$,.0f", Order_price);
        String _tax = String.format(Locale.getDefault(), "%1$,.0f", tax);
        String _Total_price = String.format(Locale.getDefault(), "%1$,.0f", Total_price);

        if (Config.ENABLE_DECIMAL_ROUNDING) {
            data_order_list += "\n" + getResources().getString(R.string.txt_order) + " " + _Order_price + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_tax) + " " + price_tax + " % : " + _tax + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_total) + " " + _Total_price + " " + str_currency_code;

            edt_order_total.setText(_Total_price + " " + str_currency_code);
            edt_total_momo.setText(String.valueOf(Total_price));

        } else {
            data_order_list += "\n" + getResources().getString(R.string.txt_order) + " " + Order_price + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_tax) + " " + str_tax + " % : " + tax + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_total) + " " + Total_price + " " + str_currency_code;

            edt_order_total.setText(Total_price + " " + str_currency_code);
            edt_total_momo.setText(String.valueOf(Total_price));
        }

        edt_order_list.setText(data_order_list);

    }

    public void dialogSuccessOrder() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.checkout_success_title);
        builder.setMessage(R.string.checkout_success_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.checkout_option_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dbhelper.addDataHistory(rand, str_order_list, str_order_total, date);
                dbhelper.deleteAllData();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder stringBuilder = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return stringBuilder.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        edt_name.setText(sharedPref.getYourName());
        edt_email.setText(sharedPref.getYourEmail());
        edt_phone.setText(sharedPref.getYourPhone());
        edt_address.setText(sharedPref.getYourAddress());
        super.onResume();
    }

    @Override
    public void receiveResultFromServer(String var1) {

    }

    private void requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
        double total_order = Double.parseDouble(edt_total_momo.getText().toString());
       long total = Math.round(total_order);
        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME, merchantName);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_CODE, merchantCode);
        eventValue.put(MoMoParameterNamePayment.AMOUNT, total);
        eventValue.put(MoMoParameterNamePayment.DESCRIPTION, str_order_list);
        //client Optional
        String fee ="0";
        eventValue.put(MoMoParameterNamePayment.FEE, fee);
        eventValue.put(MoMoParameterNamePayment.MERCHANT_NAME_LABEL, merchantNameLabel);
        eventValue.put(MoMoParameterNamePayment.REQUEST_ID,  merchantCode+"-"+ UUID.randomUUID().toString());
        eventValue.put(MoMoParameterNamePayment.PARTNER_CODE, "CGV19072017");
        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
            objExtraData.put("ticket", "{\"ticket\":{\"01\":{\"type\":\"std\",\"price\":110000,\"qty\":3}}}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put(MoMoParameterNamePayment.EXTRA_DATA, objExtraData.toString());
        eventValue.put(MoMoParameterNamePayment.REQUEST_TYPE, "payment");
        eventValue.put(MoMoParameterNamePayment.LANGUAGE, "vi");
        eventValue.put(MoMoParameterNamePayment.EXTRA, "");
        //Request momo app
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);
    }

    //Get token callback from MoMo app an submit to server side
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    requestAction2();
                    if(data.getStringExtra("data") != null && !data.getStringExtra("data").equals("")) {
                        // TODO:
                    } else {
                        Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
                    }
                } else if(data.getIntExtra("status", -1) == 1) {
                    String message = data.getStringExtra("message") != null?data.getStringExtra("message"):"Thất bại";
                    Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
                } else if(data.getIntExtra("status", -1) == 2) {
                    Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"Fail cmmn",Toast.LENGTH_SHORT).show();
        }
    }
}

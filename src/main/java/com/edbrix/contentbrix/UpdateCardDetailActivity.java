package com.edbrix.contentbrix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.utils.SessionManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.wallet.WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY;


public class UpdateCardDetailActivity extends BaseActivity {

    //private String publishableKey = "pk_test_tuGIo1fsUNf370ku8VkeXll4";
//    private String publishableKey = "pk_test_hEhCj79Onohl7VFed86jW2Ke";
    // pk_test_6pRNASCoBOKtIshFeQd4XMUh  |  sk_test_BQokikJOvBiI2HlWgH4olfQ2


    private Card payCard;
    private CardInputWidget mCardInputWidget;
    private SessionManager sessionManager;
    private Button btnUpdate;
    private Button btnCancel;
    private EditText cardDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_card_detail);
        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        cardDescription = (EditText) findViewById(R.id.cardDescription);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        sessionManager = new SessionManager(UpdateCardDetailActivity.this);
        this.initiatePaymentGateway();

        this.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePayment();
            }
        });

        this.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });

    }

    private void initiatePaymentGateway() {
        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "stripe")
                        .addParameter("stripe:publishableKey", getStripePublishableKey())
                        .addParameter("stripe:version", "4.1.5")
                        .build();

    }

    private void makePayment() {
        showBusyProgress();
        payCard = mCardInputWidget.getCard();

        if (payCard != null) {

            Stripe stripe = new Stripe(UpdateCardDetailActivity.this, getStripePublishableKey());
            stripe.createToken(
                    payCard,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            Log.d("TAG", " ID:  " + token.getId());
                            if (sessionManager.getLoggedKnowledgeBrixUserData() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId() != null && sessionManager.getLoggedKnowledgeBrixUserData().getId().length() > 0) {
                                updateSubscription(token.getId(), sessionManager.getPrefsSessionVizippToken(), sessionManager.getLoggedKnowledgeBrixUserData().getId());
                            } else {
                                hideBusyProgress();
                                showToast("Something went wrong. Please try again later..!");
                            }
                        }

                        public void onError(Exception error) {
                            // Show localized error message
//                            if (pd.isShowing())
//                                pd.dismiss();
                            hideBusyProgress();
                            showToast(error.getMessage());
                        }
                    }
            );
        } else {
            // Show errors
            Log.d("TAG", "Error: Card");
            hideBusyProgress();
            showToast("Enter valid card details.");
        }
    }

    private void updateSubscription(final String stripeToken, final String apiToken, final String userId) {

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("APITOKEN", apiToken);
        requestMap.put("stripeToken", stripeToken);
        requestMap.put("userId", userId);

        try {

            JsonObjectRequest updateSubscriptionRequest = new JsonObjectRequest(Request.Method.POST, VolleySingleton.getWsBaseUrl() + "stripegateway/updatesubscriptioncarddetails.php", new JSONObject(requestMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideBusyProgress();
                    Log.v("Volley Response", response.toString());
                    try {
                        if (response != null) {
                            if (response.has("status") && response.getString("status").equals("1")) {
                                if (response.has("message") && response.getString("message") != null)
                                    getAlertDialogManager().Dialog(getResources().getString(R.string.app_name), response.getString("message"), new AlertDialogManager.onSingleButtonClickListner() {
                                        @Override
                                        public void onPositiveClick() {
                                            finish();
                                        }
                                    }).show();
                            } else if (response.has("status") && response.getString("status").equals("0")) {
                                getAlertDialogManager().Dialog("ErrorCode : " + response.getString("errorCode"), response.getString("message"), new AlertDialogManager.onSingleButtonClickListner() {
                                    @Override
                                    public void onPositiveClick() {
                                        finish();
                                    }
                                }).show();
                            }
                        } else {
                            showToast("Something went wrong. Please try again later.");
                            finish();
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.v("Volley Excep", e.getMessage());
                        showToast("Something went wrong. Please try again later.");
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    Log.v("Volley VError", VolleySingleton.getErrorMessage(error));
                    showToast(VolleySingleton.getErrorMessage(error));
                }

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            VolleySingleton.getInstance().addToRequestQueue(updateSubscriptionRequest);
        } catch (Exception e) {
            Crashlytics.logException(e);
            hideBusyProgress();
            Log.v("Volley Excep", e.getMessage());
            showToast("Something went wrong. Please try again later.");
        }

    }

}

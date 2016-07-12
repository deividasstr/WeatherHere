package davidos.com.weatherhere.alerts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import davidos.com.weatherhere.R;

public class ConnectionToDataAlertDialogFragment extends DialogFragment {

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onCancel(dialog);
        System.exit(0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Shutting down")
                .setMessage("Currently network connection is not available. Please fix it and try again later.")
                .setPositiveButton(("Ok"), null);

        return builder.create();


    }
}

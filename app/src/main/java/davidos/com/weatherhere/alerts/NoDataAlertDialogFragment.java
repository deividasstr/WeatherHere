package davidos.com.weatherhere.alerts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class NoDataAlertDialogFragment extends android.app.DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Shutting down")
                .setMessage("Shutting down because for some reasons the data is not available." +
                        " Please check settings if localization access is allowed")
                .setPositiveButton("Ok", dialogClickListeners);

        return builder.create();

    }
    DialogInterface.OnClickListener dialogClickListeners = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            System.exit(0);
        }
    };

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        System.exit(0);
    }
}

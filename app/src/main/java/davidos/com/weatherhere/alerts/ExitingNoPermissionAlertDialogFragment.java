package davidos.com.weatherhere.alerts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;


public class ExitingNoPermissionAlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            String message = getArguments().getString("message");
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Shutting down")
                    .setMessage(message)
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

package davidos.com.weatherhere.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import davidos.com.weatherhere.R;


    public class ExitingNoPermissionAlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Shutting down")
                    .setMessage("Shutting down because cannot operate without localization access")
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
    }

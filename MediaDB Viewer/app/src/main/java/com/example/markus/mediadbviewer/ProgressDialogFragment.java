package com.example.markus.mediadbviewer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class ProgressDialogFragment extends DialogFragment {

    public int progress = 0;
    public ProgressDialog progressDialog;
    private Bundle arguments;
    private boolean instanceWasSaved = false;

    @Override
    public void setArguments(Bundle args) {
        this.arguments = args;
        super.setArguments(args);
    }

    public void incrementProgress() {

        this.progress++;
        if (this.progressDialog != null) {
            this.progressDialog.setProgress(this.progress);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("progressMax", this.arguments.getInt("progressMax"));
        outState.putInt("progress", this.progress);
        outState.putString("message", this.arguments.getString("message"));
        super.onSaveInstanceState(outState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.arguments = savedInstanceState;
            this.progress = savedInstanceState.getInt("progress");
            this.instanceWasSaved = true;
        }

        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setCancelable(false);
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.setIndeterminate(false);
        this.progressDialog.setMessage(this.arguments.getString("message"));
        this.progressDialog.setMax(this.arguments.getInt("progressMax"));
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.show();
        return this.progressDialog;
    }

}
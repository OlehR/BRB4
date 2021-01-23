package ua.uz.vopak.brb4.clientpricechecker;

import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

public class AsyncFileCheker extends AsyncTask<String , Void, Void>
{
    ClientPriceCheckerActivity activity;
    ua.uz.vopak.brb4.clientpricechecker.Config config;
    @Override
    protected Void doInBackground(String... param)
    {
        try{
            String url = "smb://10.1.0.15"+config.SmbPath; //config.SmbDomain+
            final UniAddress domainController = UniAddress.getByName("vopak.local");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("vopak.local", config.SmbUser, config.SmbPassword);
            SmbSession.logon(domainController, auth);
            SmbFile sf = new SmbFile(url, auth);
            final File destination = new File(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4.tmp");
            final File curent = new File(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4");
            sf.connect();
            String name = sf.getName();
            long d=sf.getDate();
            if(sf.getDate() > curent.lastModified()){

                if (destination.exists())
                {
                    destination.delete();
                }

                InputStream in = sf.getInputStream();
                OutputStream out = new FileOutputStream(destination);

                try {
                    // 16 kb
                    final byte[] b  = new byte[16*1024];
                    int read = 0;
                    while ((read=in.read(b, 0, b.length)) > 0) {
                        out.write(b, 0, read);
                    }
                }
                catch (Exception e){
                    e.toString();
                }
                finally {
                    in.close();
                    out.close();
                }

                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(activity.videoTimer != null){
                            activity.videoTimer.cancel();
                        }

                        if(activity.PromoVideo.isPlaying()){
                            activity.PromoVideo.stopPlayback();
                            activity.PromoVideo.setVisibility(View.INVISIBLE);
                            activity.VideoWatermark.setVisibility(View.INVISIBLE);
                        }

                        final File newDestination = new File(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4");

                        curent.delete();
                        destination.renameTo(newDestination);

                        activity.videoPlayback();
                    }
                });

            }

        }catch(Exception e){
            e.toString();
        }

        return null;
    }


    public AsyncFileCheker( ClientPriceCheckerActivity context)
    {
        activity=context;
        config = ua.uz.vopak.brb4.clientpricechecker.Config.instance(activity);
    }

}

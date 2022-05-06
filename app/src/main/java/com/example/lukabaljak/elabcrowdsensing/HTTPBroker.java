package com.example.lukabaljak.elabcrowdsensing;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.SSLCertificateSocketFactory;
import android.text.style.TabStopSpan;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HTTPBroker {

    public static String stream = null;

    public HTTPBroker() {
    }

    public static void GET(String urlString){
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            if(httpURLConnection.getResponseCode()==200){
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while((line=bufferedReader.readLine())!=null){
                    stringBuilder.append(line);
                }
                stream = stringBuilder.toString();
                httpURLConnection.disconnect();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void POST(String urlString, String json){

        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);

            byte[] out = json.getBytes();
            int length = out.length;
            httpURLConnection.setFixedLengthStreamingMode(length);
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.connect();

            //HttpsURLConnection.set

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(out);

            InputStream response = httpURLConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder odgovor = new StringBuilder();
            while((line=reader.readLine())!=null){
                odgovor.append(line);
            }
            reader.close();
            stream = odgovor.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void PUT(String urlString, String newValue){

        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setDoInput(true);

            byte[] out = newValue.getBytes();
            int length = out.length;
            httpURLConnection.setFixedLengthStreamingMode(length);
            httpURLConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(out);

            InputStream response = httpURLConnection.getInputStream();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void POSTWithCertificate(Context context,String urlString, String newValue){
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sslContext.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }


        URL url = null;
        try {
            url = new URL(urlString);


            HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
            byte[] out = newValue.getBytes();
            int length = out.length;
            httpURLConnection.setFixedLengthStreamingMode(length);
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            //httpURLConnection.setSSLSocketFactory(buildSslSocketFactory(context));
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(out);

            InputStream response = httpURLConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line;
            StringBuilder odgovor = new StringBuilder();
            while((line=reader.readLine())!=null){
                odgovor.append(line);
            }
            reader.close();
            stream = odgovor.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void postojiLiFajl(Context context){
        AssetManager mg = context.getResources().getAssets();


        InputStream is = null;
        try {
            is = mg.open("elab-ca-root.crt");
            //File exists so do something with it
            Log.d("POSTOJILI","YASSSS");
        } catch (IOException ex) {
            //file does not exist
            Log.d("POSTOJILI","NEEEE");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static SSLSocketFactory buildSslSocketFactory(Context context) {
        // Add support for self-signed (local) SSL certificates
        // Based on http://developer.android.com/training/articles/security-ssl.html#UnknownCa
        try {

            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream is = context.getResources().getAssets().open("elab-ca-root.crt");
            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                // System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            Log.d("KLJUC", ca.toString());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }



}

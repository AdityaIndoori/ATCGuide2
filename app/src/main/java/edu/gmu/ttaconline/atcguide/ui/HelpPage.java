package edu.gmu.ttaconline.atcguide.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xujiaji.happybubble.BubbleDialog;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import edu.gmu.ttaconline.atcguide.R;

/**
 * Created by yyang31 on 5/18/2018.
 */

public class HelpPage extends Activity {
    
    private ImageView back;
    private WebView webView;
    private String baseUrl = "file:///android_asset/";
    private String atUrl = "http://ttaconline.org/atsdp";
    
    private Button atInfoBtn;
    private Button atGuideBtn;

    private TextView doc;
    private TextView stuinfo;
    private TextView part1, part2;
    private TextView insArea, difficultReading;
    private TextView eligibility, considerComplete;
    private TextView curSolWorking, eligibilityResult;
    private TextView navigator, firstTiral/*, secondTrial*/;
    private TextView part3_1, /*part3_2,*/ atReferral;
    private LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /*webView = (WebView) findViewById(R.id.webview);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/help.html");*/
        
        atInfoBtn = (Button) findViewById(R.id.atinfo_btn);
        atGuideBtn = (Button) findViewById(R.id.atGuide);
        atInfoBtn.setOnClickListener(listener);
        atGuideBtn.setOnClickListener(listener);
        doc = (TextView) findViewById(R.id.doc);
        doc.setOnClickListener(listener);
        stuinfo = (TextView) findViewById(R.id.stuinfo);
        stuinfo.setOnClickListener(listener);
        part1 = (TextView) findViewById(R.id.part1);
        part1.setOnClickListener(listener);
        insArea = (TextView) findViewById(R.id.ins_area);
        insArea.setOnClickListener(listener);
        difficultReading = (TextView) findViewById(R.id.diff_reading);
        difficultReading.setOnClickListener(listener);
        part2 = (TextView) findViewById(R.id.part2);
        part2.setOnClickListener(listener);
        eligibility = (TextView) findViewById(R.id.eligibility);
        eligibility.setOnClickListener(listener);
        considerComplete = (TextView) findViewById(R.id.consider_complete);
        considerComplete.setOnClickListener(listener);
        curSolWorking = (TextView) findViewById(R.id.sol_working);
        curSolWorking.setOnClickListener(listener);
        eligibilityResult = (TextView) findViewById(R.id.eligibility_result);
        eligibilityResult.setOnClickListener(listener);
        navigator = (TextView) findViewById(R.id.navigator);
        navigator.setOnClickListener(listener);
        firstTiral = (TextView) findViewById(R.id.first_trial);
        firstTiral.setOnClickListener(listener);
        /*secondTrial = (TextView) findViewById(R.id.second_trial);
        secondTrial.setOnClickListener(listener);*/
        part3_1 = (TextView) findViewById(R.id.part3_1);
        part3_1.setOnClickListener(listener);
        /*part3_2 = (TextView) findViewById(R.id.part3_2);
        part3_2.setOnClickListener(listener);*/
        atReferral = (TextView) findViewById(R.id.at_referral);
        atReferral.setOnClickListener(listener);
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    private void CopyReadAssets()
    {
        AssetManager assetManager = getAssets();

        InputStream in = null;
        OutputStream out = null;
        File file = new File(getFilesDir(), "ATResourceGuide.pdf");
        try
        {
            in = assetManager.open("ATResourceGuide.pdf");
            out = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e)
        {
            Log.e("tag", e.getMessage());
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.parse("file://" + getFilesDir() + "/ATResourceGuide.pdf"),
                "application/pdf");

        startActivity(intent);
    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.atGuide:{
                    CopyReadAssets();
                    break;
                }
                case R.id.atinfo_btn:{
                    Uri uri = Uri.parse(atUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW,uri));
                    break;}
                case R.id.doc:
                    ImageView docImg = new ImageView(HelpPage.this);
                    docImg.setLayoutParams(imglp);
                    docImg.setImageResource(R.drawable.newdoc);
                    new BubbleDialog(HelpPage.this).addContentView(docImg).calBar(true).
                            setClickedView(doc).setPosition(BubbleDialog.Position.RIGHT).show();
                    break;
                case R.id.stuinfo:
                    ImageView stuImg = new ImageView(HelpPage.this);
                    stuImg.setLayoutParams(imglp);
                    stuImg.setImageResource(R.drawable.stuinfo);
                    new BubbleDialog(HelpPage.this).addContentView(stuImg).calBar(true).
                            setClickedView(stuinfo).setPosition(BubbleDialog.Position.RIGHT).show();
                    break;
                case R.id.ins_area:
                    ImageView areaImg = new ImageView(HelpPage.this);
                    areaImg.setLayoutParams(imglp);
                    areaImg.setImageResource(R.drawable.insarea);
                    new BubbleDialog(HelpPage.this).addContentView(areaImg).calBar(true).
                            setClickedView(insArea).setPosition(BubbleDialog.Position.RIGHT).show();
                    break;
                case R.id.diff_reading:
                    ImageView readingImg = new ImageView(HelpPage.this);
                    readingImg.setLayoutParams(imglp);
                    readingImg.setImageResource(R.drawable.diffreading);
                    new BubbleDialog(HelpPage.this).addContentView(readingImg).calBar(true).
                            setClickedView(difficultReading).setPosition(BubbleDialog.Position.RIGHT).show();
                    break;
                case R.id.part1:
                    ImageView part1Img = new ImageView(HelpPage.this);
                    part1Img.setLayoutParams(imglp);
                    part1Img.setImageResource(R.drawable.part1);
                    new BubbleDialog(HelpPage.this).addContentView(part1Img).calBar(true).
                            setClickedView(part1).setPosition(BubbleDialog.Position.RIGHT).show();
                    break;
                case R.id.part2:
                    ImageView part2Img = new ImageView(HelpPage.this);
                    part2Img.setLayoutParams(imglp);
                    part2Img.setImageResource(R.drawable.part2);
                    new BubbleDialog(HelpPage.this).addContentView(part2Img).calBar(true).
                            setClickedView(part2).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                case R.id.eligibility:
                    ImageView eliImg = new ImageView(HelpPage.this);
                    eliImg.setLayoutParams(imglp);
                    eliImg.setImageResource(R.drawable.eligible);
                    new BubbleDialog(HelpPage.this).addContentView(eliImg).calBar(true).
                            setClickedView(eligibility).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                case R.id.consider_complete:
                    ImageView compImg = new ImageView(HelpPage.this);
                    compImg.setLayoutParams(imglp);
                    compImg.setImageResource(R.drawable.complete);
                    new BubbleDialog(HelpPage.this).addContentView(compImg).calBar(true).
                            setClickedView(considerComplete).setPosition(BubbleDialog.Position.TOP).show();
                    break;
                case R.id.sol_working:
                    ImageView solImg = new ImageView(HelpPage.this);
                    solImg.setLayoutParams(imglp);
                    solImg.setImageResource(R.drawable.solworking);
                    new BubbleDialog(HelpPage.this).addContentView(solImg).calBar(true).
                            setClickedView(curSolWorking).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                case R.id.eligibility_result:
                    ImageView eligResultImg = new ImageView(HelpPage.this);
                    eligResultImg.setLayoutParams(imglp);
                    eligResultImg.setImageResource(R.drawable.eligibleresult);
                    new BubbleDialog(HelpPage.this).addContentView(eligResultImg).calBar(true).
                            setClickedView(eligibilityResult).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                case R.id.navigator:
                    /*ImageView navigatorImg = new ImageView(HelpPage.this);
                    navigatorImg.setLayoutParams(imglp);
                    navigatorImg.setImageResource(R.drawable.test);
                    new BubbleDialog(HelpPage.this).addContentView(navigatorImg).calBar(true).
                            setClickedView(navigator).setPosition(BubbleDialog.Position.LEFT).show();*/
                    break;
                case R.id.part3_1:
                    ImageView part31Img = new ImageView(HelpPage.this);
                    part31Img.setLayoutParams(imglp);
                    part31Img.setImageResource(R.drawable.part3_1);
                    new BubbleDialog(HelpPage.this).addContentView(part31Img).calBar(true).
                            setClickedView(part3_1).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                case R.id.first_trial:
                    ImageView firstTrialImg = new ImageView(HelpPage.this);
                    firstTrialImg.setLayoutParams(imglp);
                    firstTrialImg.setImageResource(R.drawable.firsttrial);
                    new BubbleDialog(HelpPage.this).addContentView(firstTrialImg).calBar(true).
                            setClickedView(firstTiral).setPosition(BubbleDialog.Position.LEFT).show();
                    break;
                /*case R.id.part3_2:
                    ImageView part32Img = new ImageView(HelpPage.this);
                    part32Img.setLayoutParams(imglp);
                    part32Img.setImageResource(R.drawable.part3_2);
                    new BubbleDialog(HelpPage.this).addContentView(part32Img).calBar(true).
                            setClickedView(part3_2).setPosition(BubbleDialog.Position.TOP).show();
                    break;
                case R.id.second_trial:
                    ImageView secondTrialImg = new ImageView(HelpPage.this);
                    secondTrialImg.setLayoutParams(imglp);
                    secondTrialImg.setImageResource(R.drawable.secondtrial);
                    new BubbleDialog(HelpPage.this).addContentView(secondTrialImg).calBar(true).
                            setClickedView(secondTrial).setPosition(BubbleDialog.Position.TOP).show();
                    break;*/
                case R.id.at_referral:
                    ImageView referralImg = new ImageView(HelpPage.this);
                    referralImg.setLayoutParams(imglp);
                    referralImg.setImageResource(R.drawable.referral);
                    new BubbleDialog(HelpPage.this).addContentView(referralImg).calBar(true).
                            setClickedView(atReferral).setPosition(BubbleDialog.Position.TOP).show();
                    break;
            }
        }
    };
}

package com.makemoji.mojilib;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import com.makemoji.mojilib.model.MojiModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * To run test, right click file in project view on the left and click Run ApplicationTest
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {

    Application application ;
    public ApplicationTest() {
        super(Application.class);
    }
    @Override
    protected void setUp()throws Exception
    {
        super.setUp();
        createApplication();
        application  = getApplication();
        Moji.initialize(getApplication(),"940ced93abf2ca4175a4a865b38f1009d8848a58");
    }
    @Before
    public void TestSimpleCreate() {
        Context context = InstrumentationRegistry.getContext();
        setContext(context);
        createApplication();
        application = getApplication();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Moji.initialize(getApplication(),"940ced93abf2ca4175a4a865b38f1009d8848a58");
        assertNotNull(application);
    }

    @Test
    public void plainTestAndBack() throws Exception {
        String html = "Test <img src=\"http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" id=\"1034\" name=\"alien\" link=\"\">";
        String plainText = Moji.htmlToPlainText(html);
        assertEquals(plainText,"Test [alien.gG]");

    }
    @Test
    public void spannedAndBack(){
        MojiModel model = new MojiModel("moji","http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png");
        model.id = 12;
        MojiSpan span1 = MojiSpan.fromModel(model,null,null);
        model.name= "roarke";
        model.id=523;
        model.link_url = "www.google.com";
        MojiSpan span2 = MojiSpan.fromModel(model,null,null);
        SpannableStringBuilder ssb = new SpannableStringBuilder("abc\uFFFC123\uFFFC");
        ssb.setSpan(span1,3,4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(span2,7,8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new MojiClickableSpan() {
            @Override
            public void onClick(View widget) {

            }
        }, 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String html = Moji.toHtml(ssb);
        Spanned newSpanned = Moji.parseHtml(html,null,true).spanned;
        assertTrue(ssb.equals(newSpanned));

    }
    @Test
    public void htmlAndBack(){
        String originalHtml = "<p dir=\"auto\" style=\"margin-bottom:16px;font-family:'.Helvetica Neue Interface';font-size:16px;\">" +
                "<span style=\"color:#000000;\">abc<img style=\"vertical-align:text-bottom;width:20px;height:20px;\" " +
                "id=\"12\"src=\"http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" name=\"moji\" link=\"\">123" +
                "<img style=\"vertical-align:text-bottom;width:20px;height:20px;\" id=\"523\"src=\"" +
                "http://d1tvcfe0bfyi6u.cloudfront.net/emoji/1034-large@2x.png\" name=\"roarke\" link=\"www.google.com\"></p>";
        Spanned spanned = Moji.parseHtml(originalHtml,null,true).spanned;
        String newHtml = Moji.toHtml(spanned);
        assertEquals(originalHtml,newHtml);

    }

}
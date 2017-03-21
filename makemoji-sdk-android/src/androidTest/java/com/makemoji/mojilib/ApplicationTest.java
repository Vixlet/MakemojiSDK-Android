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
        assertEquals(plainText,"Test [alien.Gg]");

    }
    @Test
    public void htmlPlainConsistency(){
        String plain = "[Avocado.2At][Cherry.5g] cool [Bowl of Cream.da]";
        Spanned plainSpan = Moji.plainTextToSpanned(plain);
        String html = Moji.plainTextToHtml(plain);
        ParsedAttributes spaceParse = Moji.parseHtml(html,null,true,true);
        Spanned htmlSpan = spaceParse.spanned;
        assertTrue(SSBEquals((SpannableStringBuilder)htmlSpan,plainSpan));
        String toPlain = Moji.htmlToPlainText(html);
        assertEquals(toPlain,plain);
        String htmlFromSpan = Moji.toHtml(htmlSpan);
        assertEquals(htmlFromSpan,html);
        ParsedAttributes noSpaceParse = Moji.parseHtml(html,null,true,false);
        String noSpaceHtml = Moji.toHtml(noSpaceParse.spanned);
        assertEquals(htmlFromSpan,noSpaceHtml);//html consistent no matter editText padding.

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

    public boolean SSBEquals(SpannableStringBuilder ssb, Object o){
        String s1 = ssb.toString().replace("\u0000", "");
        String s2 = o.toString().replace("\u0000", "");
        if (o instanceof Spanned &&
               s1.contentEquals(s2)) {
            Spanned other = (Spanned) o;
            // Check span data
            Object[] otherSpans = other.getSpans(0, other.length(), Object.class);
            Object[] mSpans = ssb.getSpans(0, other.length(), Object.class);
            if (mSpans.length == otherSpans.length) {
                for (int i = 0; i < mSpans.length; ++i) {
                    Object thisSpan = mSpans[i];
                    Object otherSpan = otherSpans[i];
                    if (thisSpan == this) {
                        if (other != otherSpan ||
                                ssb.getSpanStart(thisSpan) != other.getSpanStart(otherSpan) ||
                                ssb.getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) ||
                                ssb.getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                            return false;
                        }
                    } else if (
                            ssb.getSpanStart(thisSpan) != other.getSpanStart(otherSpan) ||
                            ssb.getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) ||
                            ssb.getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)
                            || !(otherSpan instanceof MojiSpan && thisSpan instanceof MojiSpan &&
                                    ((MojiSpan) otherSpan).equivelant(thisSpan))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.content.SharedPreferences;
//weiyawei add start
import java.util.Collection;
import java.util.Collections;
import java.util.List;
//weiyawei add end
import java.util.Random;
import android.widget.Toast;

import com.mediatek.launcher3.ext.LauncherLog;

import java.util.ArrayList;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
    private static final String TAG = "Launcher.Utilities";

    private static final int sIconNum = 7;
    private static final Random sRandom = new Random();
    private static Drawable[] sIconBg = null;
    private static Drawable sIconMask = null;
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
	//modify by even theme
//start
public static Bitmap getIconBitmapAsTheme(Context context, String className, Drawable defaultIcon) {
        return getIconBitmapAsTheme(context, className, defaultIcon, null);
    }

    public static Drawable getExternalDrawable(Context context, String resource) {
        return getExternalDrawable(context, resource, null);
    }

    public static Drawable getExternalDrawable(Context context, String resource, Drawable defaultDrawable) {
        PackageManager manager = context.getPackageManager();

        SharedPreferences sp = context.getSharedPreferences(Launcher.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String themePackage = sp.getString("theme", Launcher.THEME_IPHONE);

        Drawable drawable = defaultDrawable;
        if (!themePackage.equals(Launcher.THEME_DEFAULT)) {
            // get from theme
            Resources themeResources = null;
            try {
                themeResources = manager.getResourcesForApplication(themePackage);
            } catch (NameNotFoundException e) {
                //e.printStackTrace();
            }

            if (themeResources != null) {
                int resource_id = themeResources.getIdentifier(resource, "drawable", themePackage);
                if (resource_id != 0) {
                    drawable = themeResources.getDrawable(resource_id);
                }
            }
        }

        return drawable;
    }

    public static Bitmap getIconBitmapAsTheme(Context context, String className, Drawable defaultIcon, Bitmap reusedBitmap) {
		//songkun add begin
		String res = className.toLowerCase().replace(".", "_");		
		if(res.contains("$")){
			res =  res.replace("$", "_");
		}
		Drawable icon = getExternalDrawable(context, res);
		// end 
        if (icon != null) {
            if (reusedBitmap != null) reusedBitmap.eraseColor(0);

            return createIconBitmap(icon, context, false, false, null, reusedBitmap);
        }

        return null;
    }
//end
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    static int[] sLoc0 = new int[2];
    static int[] sLoc1 = new int[2];

    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    public static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    /**
     * Returns a FastBitmapDrawable with the icon, accurately sized.
     */
    public static FastBitmapDrawable createIconDrawable(Bitmap icon) {
        FastBitmapDrawable d = new FastBitmapDrawable(icon);
        d.setFilterBitmap(true);
        resizeIconDrawable(d);
        return d;
    }

    /**
     * Resizes an icon drawable to the correct icon size.
     */
    static void resizeIconDrawable(Drawable icon) {
        icon.setBounds(0, 0, sIconWidth, sIconHeight);
    }

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static boolean isRotationEnabled(Context c) {
        boolean enableRotation = sForceEnableRotation ||
                c.getResources().getBoolean(R.bool.allow_rotation);
        //Set can not cross screen launcher by lixiang 20160512
        //return enableRotation;
        return false;
    }

    /**
     * Indicates if the device is running LMP or higher.
     */
    public static boolean isLmpOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Returns a bitmap suitable for the all apps view. If the package or the resource do not
     * exist, it returns null.
     */
    static Bitmap createIconBitmap(String packageName, String resourceName, IconCache cache,
            Context context) {
        PackageManager packageManager = context.getPackageManager();
        // the resource
        try {
            Resources resources = packageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);
                return createIconBitmap(
                        resources.getDrawableForDensity(id, cache.getFullResIconDpi()), context);
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }
			 //modify by even theme
	 //start
	     static Bitmap createIconBitmap(Drawable icon, Context context) {
        return createIconBitmap(icon, context, null);
    }


    static int getIconColor(Context context, String className) {
        if (className == null) return -1;

        Cursor c = null;
        try {
            final ContentResolver cr = context.getContentResolver();
            c = cr.query(LauncherSettings.IconColor.CONTENT_URI,
                    new String[] { "color" }, "name=?",
                    new String[] { className }, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return -1;
    }

    static void insertIconColor(Context context, String className, int color) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        values.put(LauncherSettings.IconColor._ID, ItemInfo.NO_ID);
        values.put(LauncherSettings.IconColor.NAME, className);
        values.put(LauncherSettings.IconColor.COLOR, color);

        cr.insert(LauncherSettings.IconColor.CONTENT_URI, values);
    }

    static Bitmap createIconBitmap(Drawable icon, Context context, String className) {
        SharedPreferences sp = context.getSharedPreferences(Launcher.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String themePackage = sp.getString("theme", Launcher.THEME_IPHONE);
        if (themePackage.equals(Launcher.THEME_DEFAULT)) {
        	return createIconBitmap(icon, context, false, false, className);
        } else {
        	return createIconBitmap(icon, context, true, true, className);
        }
    }

    static Bitmap createIconBitmap(Drawable icon, Context context, boolean draw_bg, boolean draw_mask, String className) {
        return createIconBitmap(icon, context, draw_bg, draw_mask, className, null);
    }
	 //end
    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an icon
     */
    static Bitmap createIconBitmap(Bitmap icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
        }
        if (sIconWidth == icon.getWidth() && sIconHeight == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), icon), context);
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context, boolean draw_bg, boolean draw_mask, String className, Bitmap reusedBitmap) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;
            Bitmap iconBitmap = null;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                iconBitmap = bitmap;
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
					//hejianfeng add start 
					height=height*7/10;
					width=width*7/10;
					//hejianfeng add end 
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
	    //weiyawei modify start
            int textureWidth = sIconWidth;
            int textureHeight = sIconHeight;
	    //weiyawei modify end
            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            int left = (textureWidth-width) / 2;
            int top = (textureHeight-height) / 2;

            if (false) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }
//modify by even theme
            draw_bg = false;//weiyawei add
            if (draw_bg) {
                if (sIconBg == null) {
                    sIconBg = new Drawable[sIconNum];
                    sIconBg[0] = getExternalDrawable(context, "icon_mask0_bg");
                    sIconBg[1] = getExternalDrawable(context, "icon_mask1_bg");
                    sIconBg[2] = getExternalDrawable(context, "icon_mask2_bg");
                    sIconBg[3] = getExternalDrawable(context, "icon_mask3_bg");
                    sIconBg[4] = getExternalDrawable(context, "icon_mask4_bg");
                    sIconBg[5] = getExternalDrawable(context, "icon_mask5_bg");
                    sIconBg[6] = getExternalDrawable(context, "icon_mask6_bg");
                }

                int color = getIconColor(context, className);
                if (color < 0) {
                    color = sRandom.nextInt(sIconNum);
                    if(className!=null){
                    insertIconColor(context, className, color);
                    }
                }

                Drawable bg = sIconBg[color];
                if (bg != null) {
                    sOldBounds.set(bg.getBounds());
                    bg.setBounds(0, 0, textureWidth, textureHeight);
                    bg.draw(canvas);
                    bg.setBounds(sOldBounds);
                }
            } else {
                /* if do not draw bg, fill the icon to whole filed. */
                left = 0;
                top = 0;
                width = textureWidth;
                height = textureHeight;
            }
//end
            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
		//weiyawei modify start
		// icon.setBounds(left, top, left+width, top+height);
            boolean ap = true;
            if(className != null) {
                ap = isSystemApp(context, className);
            }
            if("com.android.calculator2.Calculator".equals(className) ||
                    "com.android.quicksearchbox.SearchActivity".equals(className) ||
                    "com.android.fmradio.FmMainActivity".equals(className) ||
                    "com.android.stk.StkMain".equals(className) ||
                    "com.android.soundrecorder.SoundRecorder".equals(className)||
                    "com.google.android.maps.MapsActivity".equals(className) ||
                    "com.android.providers.downloads.ui.DownloadList".equals(className)){
                icon.setBounds(left, top, left+width, top+height);
            }else if(!ap){
                icon.setBounds(left, top, 150, 150);
            }else{
                icon.setBounds(left, top, left+width, top+height);
            }

       /* if(className != null) {
            ap = isSystemApp(context, className);
        }
            android.util.Log.i("weiyawei5", "className == " + className + " ap == " + ap);
        if("com.android.calendar.AllInOneActivity".equals(className)  ){
			icon.setBounds(left, top, 185, 185);
		}else if("com.elephone.compass.CompassActivity".equals(className) ||
                "com.elephone.elephoneuserfeedbackv1.StartActivity".equals(className)){
			icon.setBounds(left, top, 200, 200);
        }else if("com.mediatek.filemanager.FileManagerOperationActivity".equals(className) ||
                "com.android.mms.ui.BootActivity".equals(className)){
            icon.setBounds(left, top, 240, 240);
        }else if("com.android.calculator2.Calculator".equals(className) ||
                "com.android.quicksearchbox.SearchActivity".equals(className) ||
                "com.android.fmradio.FmMainActivity".equals(className) ||
                "com.android.stk.StkMain".equals(className) ||
                "com.android.soundrecorder.SoundRecorder".equals(className)||
                "com.google.android.maps.MapsActivity".equals(className) ||
                "com.android.providers.downloads.ui.DownloadList".equals(className)){
                icon.setBounds(left, top, 210, 210);
		}else if(!ap){
            icon.setBounds(left, top, 180, 180);
        }else{
			icon.setBounds(left, top, 210, 210);
		}*/
		//weiyawei modify end
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
//modify by even theme
            if (draw_mask) {
                if (sIconMask == null) {
                    sIconMask = getExternalDrawable(context, "icon_mask_front");
                }

                Drawable mask = sIconMask;

                if (mask != null) {
                    sOldBounds.set(mask.getBounds());
                    mask.setBounds(0, 0, textureWidth, textureHeight);
                    mask.draw(canvas);
                    mask.setBounds(sOldBounds);
                }
            }
//end
            canvas.setBitmap(null);

            return bitmap;
        }
    }
    //weiyawei add start
    public static boolean isSystemApp(Context context,String className){
        PackageManager pm = context.getPackageManager(); //获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo reInfo : resolveInfos) {
            String packageName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            String activityName = reInfo.activityInfo.name;
            android.util.Log.i("weiyawei3", "activityName == " + activityName);
            if(className.equals(activityName)){
                return true;
            }
        }
        return false;
    }
    //weiyawei add end

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param root The root view to make the coordinates relative to.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant, View root,
                                                           int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i-1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    /// M: Change to public for smart book feature.
    public static void initStatics(Context context) {
        final Resources resources = context.getResources();
        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
    }

    public static void setIconSize(int widthPx) {
        sIconWidth = sIconHeight = widthPx;
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    public static int[] getCenterDeltaInScreenSpace(View v0, View v1, int[] delta) {
        v0.getLocationInWindow(sLoc0);
        v1.getLocationInWindow(sLoc1);

        sLoc0[0] += (v0.getMeasuredWidth() * v0.getScaleX()) / 2;
        sLoc0[1] += (v0.getMeasuredHeight() * v0.getScaleY()) / 2;
        sLoc1[0] += (v1.getMeasuredWidth() * v1.getScaleX()) / 2;
        sLoc1[1] += (v1.getMeasuredHeight() * v1.getScaleY()) / 2;

        if (delta == null) {
            delta = new int[2];
        }

        delta[0] = sLoc1[0] - sLoc0[0];
        delta[1] = sLoc1[1] - sLoc0[1];

        return delta;
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utilities.scaleRect(r, scale);
        r.offset(cx, cy);
    }

    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     * @param bitmap The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<Float>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Failed to find resources for " + packageName);
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isViewAttachedToWindow(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return v.isAttachedToWindow();
        } else {
            // A proxy call which returns null, if the view is not attached to the window.
            return v.getKeyDispatcherState() != null;
        }
    }

    /**
     * Returns a widget with category {@link AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}
     * provided by the same package which is set to be global search activity.
     * If widgetCategory is not supported, or no such widget is found, returns the first widget
     * provided by the package.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static AppWidgetProviderInfo getSearchWidgetProvider(Context context) {
        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchComponent = searchManager.getGlobalSearchActivity();
        if (searchComponent == null) return null;
        String providerPkg = searchComponent.getPackageName();

        AppWidgetProviderInfo defaultWidgetForSearchPackage = null;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
            if (info.provider.getPackageName().equals(providerPkg)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if ((info.widgetCategory & AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX) != 0) {
                        return info;
                    } else if (defaultWidgetForSearchPackage == null) {
                        defaultWidgetForSearchPackage = info;
                    }
                } else {
                    return info;
                }
            }
        }
        return defaultWidgetForSearchPackage;
    }

    /**
     * M: Check whether the given component name is enabled.
     *
     * @param context
     * @param cmpName
     * @return true if the component is in default or enable state, and the application is also in default or enable state,
     *         false if in disable or disable user state.
     */
    static boolean isComponentEnabled(final Context context, final ComponentName cmpName) {
        final String pkgName = cmpName.getPackageName();
        final PackageManager pm = context.getPackageManager();
        // Check whether the package has been uninstalled or the component already removed.
        ActivityInfo aInfo = null;
        try {
            aInfo = pm.getActivityInfo(cmpName, 0);
        } catch (NameNotFoundException e) {
            LauncherLog.w(TAG, "isComponentEnabled NameNotFoundException: pkgName = " + pkgName);
        }

        if (aInfo == null) {
            LauncherLog.d(TAG, "isComponentEnabled return false because component " + cmpName + " has been uninstalled!");
            return false;
        }

        final int pkgEnableState = pm.getApplicationEnabledSetting(pkgName);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isComponentEnabled: cmpName = " + cmpName + ",pkgEnableState = " + pkgEnableState);
        }
        if (pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            final int cmpEnableState = pm.getComponentEnabledSetting(cmpName);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "isComponentEnabled: cmpEnableState = " + cmpEnableState);
            }
            if (cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    || cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true;
            }
        }

        return false;
    }

    /**
     * M: The app is system app or not.
     *
     * @param info
     * @return
     */
    public static boolean isSystemApp(AppInfo info) {
        if (info == null) {
            return false;
        }
        return (info.flags & AppInfo.DOWNLOADED_FLAG) == 0;
    }
}

package com.exoplayer.video;


import static com.google.android.exoplayer2.Player.COMMAND_GET_CURRENT_MEDIA_ITEM;
import static com.google.android.exoplayer2.Player.COMMAND_GET_MEDIA_ITEMS_METADATA;
import static com.google.android.exoplayer2.Player.COMMAND_GET_TEXT;
import static com.google.android.exoplayer2.Player.COMMAND_GET_TIMELINE;
import static com.google.android.exoplayer2.Player.COMMAND_GET_TRACKS;
import static com.google.android.exoplayer2.Player.COMMAND_SET_VIDEO_SURFACE;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import static java.lang.annotation.ElementType.TYPE_USE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.exoplayer.exolibrary.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Timeline.Period;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.text.CueGroup;
import com.google.android.exoplayer2.ui.AdOverlayInfo;
import com.google.android.exoplayer2.ui.AdViewProvider;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 一个高层视图{@link Player}媒体播放。它可以显示视频、字幕和专辑图片
 * <p>
 * 在播放期间，并使用{@link StyledPlayerControlView}显示播放控件。
 * <p>
 * ＊
 * <p>
 * StyledPlayerView可以通过设置属性(或调用相应的方法)来定制，或重写drawables。
 * <p>
 * ＊
 * <p>
 * < / h2 * < h2 >属性>
 *
 * <p>
 * <p>
 * 在布局XML文件中使用时，可以在StyledPlayerView上设置以下属性:
 * <p>
 * ＊
 * <p>
 * < ul >
 *
 * <li><b>{@code use_artwork}</b> -如果音频流中可用，是否使用artwork(封面)。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setUseArtwork(boolean)}
 *
 * <li>默认值:{@code true}
 * <p>
 * </ ul >
 *
 * <li><b>{@code default_artwork}</b> -如果音频中没有可用的artwork，则使用默认的artwork
 * <p>
 * 流。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setDefaultArtwork(Drawable)}
 *
 * <li>默认值:{@code null}
 * <p>
 * </ ul >
 *
 * <li><b>{@code use_controller}</b> -是否可以显示控制器。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setUseController(boolean)}
 *
 * <li>默认值:{@code true}
 * <p>
 * </ ul >
 *
 * <li><b>{@code hide_on_touch}</b> -控制器是否被触摸事件隐藏。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setControllerHideOnTouch(boolean)}
 *
 * <li>默认值:{@code true}
 * <p>
 * </ ul >
 *
 * <li><b>{@code auto_show}</b> -控制器是否自动显示
 * <p>
 * 播放开始，暂停，结束或失败。如果设置为false，则控制器可以为
 * <p>
 * 使用{@link #showController()}和{@link #hideController()}手动操作。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setControllerAutoShow(boolean)}
 *
 * <li>默认值:{@code true}
 * <p>
 * </ ul >
 *
 * <li><b>{@code hide_during_ads}</b> -是否在广告期间隐藏播放控件。
 * <p>
 * 控件总是在广告期间显示，如果它们被启用，玩家被暂停。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setControllerHideDuringAds(boolean)}
 *
 * <li>默认值:{@code true}
 * <p>
 * </ ul >
 *
 * <li><b>{@code show_buffering}</b> -当播放器
 * <p>
 * 正在缓冲。有效值为{@code never}、{@code when_playing}和{@code always}。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setShowBuffering(int)}
 *
 * <li>默认值:{@code never}
 * <p>
 * </ ul >
 *
 * <li><b>{@code resize_mode}</b> -控制视频和专辑艺术如何在视图中调整大小。
 * <p>
 * 有效值为{@code fit}， {@code fixed_width}， {@code fixed_height}， {@code fill}和
 * <p>
 * {@code缩放}。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setResizeMode(int)}
 *
 * <li>默认值:{@code fit}
 * <p>
 * </ ul >
 *
 * <li><b>{@code surface_type}</b> -用于视频回放的表面视图类型。有效的
 * <p>
 * 值为{@code surface_view}， {@code texture_view}， {@code spherical_gl_surface_view}，
 * <p>
 * {@code video_decoder_gl_surface_view}和{@code none}。建议使用{@code none}
 * <p>
 * 对于音频应用程序，因为创建表面可能是昂贵的。使用{@code
 * <p>
 * surface_view}建议用于视频应用。注意，TextureView只能在
 * <p>
 * 硬件加速窗口。当在软件中渲染时，TextureView将什么都不画。
 * <p>
 * < ul >
 *
 * <li>对应方法:无
 *
 * <li>默认值:{@code surface_view}
 * <p>
 * </ ul >
 *
 * <li><b>{@code shutter_background_color}</b> - {@code exo_shutter}的背景色
 * <p>
 * 视图。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setShutterBackgroundColor(int)}
 *
 * <li>默认值:{@code unset}
 * <p>
 * </ ul >
 *
 * <li><b>{@code keep_content_on_player_reset}</b> -当前是否显示视频帧
 * <p>
 * 或媒体艺术品保持可见时，玩家重置。
 * <p>
 * < ul >
 *
 * <li>对应方法:{@link #setKeepContentOnPlayerReset(boolean)}
 *
 * <li>默认值:{@code false}
 * <p>
 * </ ul >
 *
 * <li>可以在{@link StyledPlayerControlView}和{@link . view上设置的所有属性
 * <p>
 * DefaultTimeBar}也可以在StyledPlayerView上设置，并将被传播到
 * <p>
 * 膨胀{@link StyledPlayerControlView}。
 * <p>
 * < / ul >
 * <p>
 * ＊
 *
 * <h2>覆盖可提款</h2>
 *
 * <p>
 * <p>
 * {@link StyledPlayerControlView}使用的drawables可以被
 * <p>
 * 在应用程序中定义的相同名称。参见{@link StyledPlayerControlView}文档
 * <p>
 * 可覆盖的绘图列表。
 */
public class ExoVideoView extends FrameLayout implements AdViewProvider {

    /**
     * 要通知有关 UI 控件可见性更改的侦听器.
     */
    public interface ControllerVisibilityListener {

        /**
         * 可见性更改时调用.
         *
         * @param visibility 新的可见性。{@link View#VISIBLE} 或 {@link View#GONE}。
         */
        void onVisibilityChanged(int visibility);
    }

    /**
     * 单击全屏按钮时调用的侦听器。该实现负责更改 UI 布局。
     */
    public interface FullscreenButtonClickListener {
        /**
         * 单击全屏按钮时调用.
         *
         * @param isFullScreen {@code true} 如果视频呈现图面应为全屏，则为 {@code false}。
         */
        void onFullscreenButtonClick(boolean isFullScreen);
    }

    /**
     * 确定何时显示缓冲视图。{@link #SHOW_BUFFERING_NEVER}、{@link #SHOW_BUFFERING_WHEN_PLAYING} 或 {@link #SHOW_BUFFERING_ALWAYS} 之一。
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target(TYPE_USE)
    @IntDef({SHOW_BUFFERING_NEVER, SHOW_BUFFERING_WHEN_PLAYING, SHOW_BUFFERING_ALWAYS})
    public @interface ShowBuffering {
    }

    /**
     * 缓冲视图从不显示.
     */
    public static final int SHOW_BUFFERING_NEVER = 0;
    /**
     * 当播放器处于 {@link Player#STATE_BUFFERING 缓冲} 状态并且 {@link Player#getPlayWhenReady（） playWhenReady} 为 {@code true} 时，将显示缓冲视图。
     */
    public static final int SHOW_BUFFERING_WHEN_PLAYING = 1;
    /**
     * 当播放器处于 {@link Player#STATE_BUFFERING 缓冲} 状态时，始终显示缓冲视图。
     */
    public static final int SHOW_BUFFERING_ALWAYS = 2;

    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;
    private static final int SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW = 3;
    private static final int SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW = 4;

    private final ComponentListener componentListener;
    @Nullable
    private final AspectRatioFrameLayout contentFrame;
    @Nullable
    private final View shutterView;
    @Nullable
    private final View surfaceView;
    private final boolean surfaceViewIgnoresVideoAspectRatio;
    /**
     * 封面图
     **/
    @Nullable
    private final ImageView artworkView;
    /**
     * 字幕视图
     **/
    @Nullable
    private final SubtitleView subtitleView;
    @Nullable
    private final View bufferingView;
    @Nullable
    private final TextView errorMessageView;
    @Nullable
    private final StyledPlayerControlView controller;
    /**
     * 广告占位图
     **/
    @Nullable
    private final FrameLayout adOverlayFrameLayout;
    @Nullable
    private final FrameLayout overlayFrameLayout;

    @Nullable
    private Player player;
    private boolean useController;

    // 最多一个 controllerVisibilityListener 和 legacyControllerVisibilityListener 是非空的.
    @Nullable
    private ControllerVisibilityListener controllerVisibilityListener;

    @SuppressWarnings("deprecation")
    @Nullable
    private StyledPlayerControlView.VisibilityListener legacyControllerVisibilityListener;

    @Nullable
    private FullscreenButtonClickListener fullscreenButtonClickListener;

    private boolean useArtwork;
    @Nullable
    private Drawable defaultArtwork;
    private @ShowBuffering int showBuffering;
    /**
     * 视频最后一帧是否可见
     **/
    private boolean keepContentOnPlayerReset;
    @Nullable
    private ErrorMessageProvider<? super PlaybackException> errorMessageProvider;
    @Nullable
    private CharSequence customErrorMessage;
    private int controllerShowTimeoutMs;
    private boolean controllerAutoShow;
    private boolean controllerHideDuringAds;
    private boolean controllerHideOnTouch;
    private int textureViewRotation;


    public ExoVideoView(Context context) {
        this(context, null);
    }

    public ExoVideoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings({"nullness:argument", "nullness:method.invocation"})
    public ExoVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        componentListener = new ComponentListener();

        if (isInEditMode()) {
            contentFrame = null;
            shutterView = null;
            surfaceView = null;
            surfaceViewIgnoresVideoAspectRatio = false;
            artworkView = null;
            subtitleView = null;
            bufferingView = null;
            errorMessageView = null;
            controller = null;
            adOverlayFrameLayout = null;
            overlayFrameLayout = null;
            return;
        }

        boolean shutterColorSet = false;
        int shutterColor = 0;
        int playerLayoutId = R.layout.exo_video_view;
        boolean useArtwork = true;
        int defaultArtworkId = 0;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS;
        boolean controllerHideOnTouch = true;
        boolean controllerAutoShow = true;
        boolean controllerHideDuringAds = true;
        int showBuffering = SHOW_BUFFERING_NEVER;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoVideoView, defStyleAttr, /* defStyleRes= */ 0);
            try {
                shutterColorSet = a.hasValue(R.styleable.ExoVideoView_shutter_background_color);
                shutterColor = a.getColor(R.styleable.ExoVideoView_shutter_background_color, shutterColor);
                playerLayoutId = a.getResourceId(R.styleable.ExoVideoView_player_layout_id, playerLayoutId);
                useArtwork = a.getBoolean(R.styleable.ExoVideoView_use_artwork, useArtwork);
                defaultArtworkId = a.getResourceId(R.styleable.ExoVideoView_default_artwork, defaultArtworkId);
                useController = a.getBoolean(R.styleable.ExoVideoView_use_controller, useController);
                surfaceType = a.getInt(R.styleable.ExoVideoView_surface_type, surfaceType);
                resizeMode = a.getInt(R.styleable.ExoVideoView_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(R.styleable.ExoVideoView_show_timeout, controllerShowTimeoutMs);
                controllerHideOnTouch = a.getBoolean(R.styleable.ExoVideoView_hide_on_touch, controllerHideOnTouch);
                controllerAutoShow = a.getBoolean(R.styleable.ExoVideoView_auto_show, controllerAutoShow);
                showBuffering = a.getInteger(R.styleable.ExoVideoView_show_buffering, showBuffering);
                keepContentOnPlayerReset = a.getBoolean(R.styleable.ExoVideoView_keep_content_on_player_reset, keepContentOnPlayerReset);
                controllerHideDuringAds = a.getBoolean(R.styleable.ExoVideoView_hide_during_ads, controllerHideDuringAds);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(playerLayoutId, this);
        //设置子体可聚焦性
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = findViewById(R.id.exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(R.id.exo_shutter);
        if (shutterView != null && shutterColorSet) {
            shutterView.setBackgroundColor(shutterColor);
        }

        // 创建曲面视图并将其插入到内容框架中（如果有）。
        boolean surfaceViewIgnoresVideoAspectRatio = false;
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            switch (surfaceType) {
                case SURFACE_TYPE_TEXTURE_VIEW:
                    surfaceView = new TextureView(context);
                    break;
                case SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW:
                    try {
                        Class<?> clazz = Class.forName("com.google.android.exoplayer2.video.spherical.SphericalGLSurfaceView");
                        surfaceView = (View) clazz.getConstructor(Context.class).newInstance(context);
                    } catch (Exception e) {
                        throw new IllegalStateException("spherical_gl_surface_view requires an ExoPlayer dependency", e);
                    }
                    surfaceViewIgnoresVideoAspectRatio = true;
                    break;
                case SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW:
                    try {
                        Class<?> clazz = Class.forName("com.google.android.exoplayer2.video.VideoDecoderGLSurfaceView");
                        surfaceView = (View) clazz.getConstructor(Context.class).newInstance(context);
                    } catch (Exception e) {
                        throw new IllegalStateException("video_decoder_gl_surface_view requires an ExoPlayer dependency", e);
                    }
                    break;
                default:
                    surfaceView = new SurfaceView(context);
                    break;
            }
            surfaceView.setLayoutParams(params);
            // 我们不希望 surfaceView 可单独点击到 StyledPlayerView 本身，
            // 但我们确实希望注册为 OnClickListener，
            // 以便 surfaceView 实现可以通过调用自己的 performClick 方法将点击事件传播到 StyledPlayerView。
            surfaceView.setOnClickListener(componentListener);
            surfaceView.setClickable(false);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }
        this.surfaceViewIgnoresVideoAspectRatio = surfaceViewIgnoresVideoAspectRatio;

        // Ad overlay frame layout.
        adOverlayFrameLayout = findViewById(R.id.exo_ad_overlay);

        // Overlay frame layout.
        overlayFrameLayout = findViewById(R.id.exo_overlay);

        // Artwork view.
        artworkView = findViewById(R.id.exo_artwork);
        this.useArtwork = useArtwork && artworkView != null;
        if (defaultArtworkId != 0) {
            defaultArtwork = ContextCompat.getDrawable(getContext(), defaultArtworkId);
        }

        // Subtitle view.
        subtitleView = findViewById(R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }

        // Buffering view.
        bufferingView = findViewById(R.id.exo_buffering);
        if (bufferingView != null) {
            bufferingView.setVisibility(View.GONE);
        }
        this.showBuffering = showBuffering;

        // Error message view.
        errorMessageView = findViewById(R.id.exo_error_message);
        if (errorMessageView != null) {
            errorMessageView.setVisibility(View.GONE);
        }

        // Playback control view.
        StyledPlayerControlView customController = findViewById(R.id.exo_controller);
        View controllerPlaceholder = findViewById(R.id.exo_controller_placeholder);
        if (customController != null) {
            this.controller = customController;
        } else if (controllerPlaceholder != null) {
            //将 attr 传播为播放收件人，以便传输 StyledPlayerControlView 的自定义属性，但不会传输标准属性（例如背景）。
            this.controller = new StyledPlayerControlView(context, null, 0, attrs);
            controller.setId(R.id.exo_controller);
            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(controller, controllerIndex);
        } else {
            this.controller = null;
        }
        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.controllerHideOnTouch = controllerHideOnTouch;
        this.controllerAutoShow = controllerAutoShow;
        this.controllerHideDuringAds = controllerHideDuringAds;
        this.useController = useController && controller != null;
        if (controller != null) {
            controller.hideImmediately();
            controller.addVisibilityListener(/* listener= */ componentListener);
        }
        if (useController) {
            setClickable(true);
        }
        updateContentDescription();
    }

    /**
     * Switches the view targeted by a given {@link Player}.
     *
     * @param player        The player whose target view is being switched.
     * @param oldPlayerView The old view to detach from the player.
     * @param newPlayerView The new view to attach to the player.
     */
    public static void switchTargetView(Player player, @Nullable com.google.android.exoplayer2.ui.StyledPlayerView oldPlayerView, @Nullable com.google.android.exoplayer2.ui.StyledPlayerView newPlayerView) {
        if (oldPlayerView == newPlayerView) {
            return;
        }
        // We attach the new view before detaching the old one because this ordering allows the player
        // to swap directly from one surface to another, without transitioning through a state where no
        // surface is attached. This is significantly more efficient and achieves a more seamless
        // transition when using platform provided video decoders.
        if (newPlayerView != null) {
            newPlayerView.setPlayer(player);
        }
        if (oldPlayerView != null) {
            oldPlayerView.setPlayer(null);
        }
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Player} to use.
     *
     * <p>To transition a {@link Player} from targeting one view to another, it's recommended to use
     * {@link #switchTargetView(Player, com.google.android.exoplayer2.ui.StyledPlayerView, com.google.android.exoplayer2.ui.StyledPlayerView)} rather than this method.
     * If you do wish to use this method directly, be sure to attach the player to the new view
     * <em>before</em> calling {@code setPlayer(null)} to detach it from the old one. This ordering is
     * significantly more efficient and may allow for more seamless transitions.
     *
     * @param player The {@link Player} to use, or {@code null} to detach the current player. Only
     *               players which are accessed on the main thread are supported ({@code
     *               player.getApplicationLooper() == Looper.getMainLooper()}).
     */
    public void setPlayer(@Nullable Player player) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
        Assertions.checkArgument(player == null || player.getApplicationLooper() == Looper.getMainLooper());
        if (this.player == player) {
            return;
        }
        @Nullable Player oldPlayer = this.player;
        if (oldPlayer != null) {
            oldPlayer.removeListener(componentListener);
            if (oldPlayer.isCommandAvailable(COMMAND_SET_VIDEO_SURFACE)) {
                if (surfaceView instanceof TextureView) {
                    oldPlayer.clearVideoTextureView((TextureView) surfaceView);
                } else if (surfaceView instanceof SurfaceView) {
                    oldPlayer.clearVideoSurfaceView((SurfaceView) surfaceView);
                }
            }
        }
        if (subtitleView != null) {
            subtitleView.setCues(null);
        }
        this.player = player;
        if (useController()) {
            if (controller != null) {
                controller.setPlayer(player);
            }
        }
        updateBuffering();
        updateErrorMessage();
        updateForCurrentTrackSelections(/* isNewPlayer= */ true);
        if (player != null) {
            if (player.isCommandAvailable(COMMAND_SET_VIDEO_SURFACE)) {
                if (surfaceView instanceof TextureView) {
                    player.setVideoTextureView((TextureView) surfaceView);
                } else if (surfaceView instanceof SurfaceView) {
                    player.setVideoSurfaceView((SurfaceView) surfaceView);
                }
                updateAspectRatio();
            }
            if (subtitleView != null && player.isCommandAvailable(COMMAND_GET_TEXT)) {
                subtitleView.setCues(player.getCurrentCues().cues);
            }
            player.addListener(componentListener);
            maybeShowController(false);

        } else {
            hideController();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (surfaceView instanceof SurfaceView) {
            // Work around https://github.com/google/ExoPlayer/issues/3160.
            surfaceView.setVisibility(visibility);
        }
    }

    /**
     * Sets the {@link ResizeMode}.
     *
     * @param resizeMode The {@link ResizeMode}.
     */
    public void setResizeMode(@ResizeMode int resizeMode) {
        Assertions.checkStateNotNull(contentFrame);
        if (contentFrame != null) {
            contentFrame.setResizeMode(resizeMode);
        }

    }

    /**
     * Returns the {@link ResizeMode}.
     */
    public @ResizeMode int getResizeMode() {
        Assertions.checkStateNotNull(contentFrame);
        return contentFrame != null ? contentFrame.getResizeMode() : RESIZE_MODE_FIT;
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    public void setUseArtwork(boolean useArtwork) {
        Assertions.checkState(!useArtwork || null != artworkView);
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork;
            updateForCurrentTrackSelections(/* isNewPlayer= */ false);
        }
    }

    /**
     * Returns the default artwork to display.
     */
    @Nullable
    public Drawable getDefaultArtwork() {
        return defaultArtwork;
    }

    /**
     * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display
     */
    public void setDefaultArtwork(@Nullable Drawable defaultArtwork) {
        if (this.defaultArtwork != defaultArtwork) {
            this.defaultArtwork = defaultArtwork;
            updateForCurrentTrackSelections(/* isNewPlayer= */ false);
        }
    }

    /**
     * Returns whether the playback controls can be shown.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether the playback controls can be shown. If set to {@code false} the playback controls
     * are never visible and are disconnected from the player.
     *
     * <p>This call will update whether the view is clickable. After the call, the view will be
     * clickable if playback controls can be shown or if the view has a registered click listener.
     *
     * @param useController Whether the playback controls can be shown.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controller != null);
        setClickable(useController || hasOnClickListeners());
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController()) {
            if (controller != null) {
                controller.setPlayer(player);
            }
        } else if (controller != null) {
            controller.hide();
            controller.setPlayer(/* player= */ null);
        }
        updateContentDescription();
    }

    /**
     * Sets the background color of the {@code exo_shutter} view.
     *
     * @param color The background color.
     */
    public void setShutterBackgroundColor(@ColorInt int color) {
        if (shutterView != null) {
            shutterView.setBackgroundColor(color);
        }
    }

    /**
     * 设置重置播放器时当前显示的视频帧或媒体图稿是否保持可见。播放器重置定义为使用不同媒体重新准备的播放器、
     * 转换到未准备好的媒体或空媒体项列表的播放器，或者通过调用替换或清除的播放器 {@link #setPlayer(Player)}.
     *
     * <p>如果启用，当前显示的视频帧或媒体图稿将保持可见，直到视图上的播放器成功准备好新媒体并加载足够的新媒体以确定可用的轨道。
     * 因此，启用此选项允许从播放一个媒体过渡到另一个媒体，或从一个播放器实例转换到另一个播放器实例，而无需清除视图的内容.
     *
     * <p>如果禁用，播放器重置后，当前显示的视频帧或媒体插图将被隐藏。
     * 请注意，通过使 {@code exo_shutter} 可见来隐藏视频帧。因此，如果使用省略此视图的自定义布局，则不会隐藏视频帧。
     *
     * @param keepContentOnPlayerReset Whether the currently displayed video frame or media artwork is
     *                                 kept visible when the player is reset.
     */
    public void setKeepContentOnPlayerReset(boolean keepContentOnPlayerReset) {
        if (this.keepContentOnPlayerReset != keepContentOnPlayerReset) {
            this.keepContentOnPlayerReset = keepContentOnPlayerReset;
            updateForCurrentTrackSelections(/* isNewPlayer= */ false);
        }
    }

    /**
     * Sets whether a buffering spinner is displayed when the player is in the buffering state. The
     * buffering spinner is not displayed by default.
     *
     * @param showBuffering The mode that defines when the buffering spinner is displayed. One of
     *                      {@link #SHOW_BUFFERING_NEVER}, {@link #SHOW_BUFFERING_WHEN_PLAYING} and {@link
     *                      #SHOW_BUFFERING_ALWAYS}.
     */
    public void setShowBuffering(@ShowBuffering int showBuffering) {
        if (this.showBuffering != showBuffering) {
            this.showBuffering = showBuffering;
            updateBuffering();
        }
    }

    /**
     * Sets the optional {@link ErrorMessageProvider}.
     *
     * @param errorMessageProvider The error message provider.
     */
    public void setErrorMessageProvider(@Nullable ErrorMessageProvider<? super PlaybackException> errorMessageProvider) {
        if (this.errorMessageProvider != errorMessageProvider) {
            this.errorMessageProvider = errorMessageProvider;
            updateErrorMessage();
        }
    }

    /**
     * Sets a custom error message to be displayed by the view. The error message will be displayed
     * permanently, unless it is cleared by passing {@code null} to this method.
     *
     * @param message The message to display, or {@code null} to clear a previously set message.
     */
    public void setCustomErrorMessage(@Nullable CharSequence message) {
        Assertions.checkState(errorMessageView != null);
        customErrorMessage = message;
        updateErrorMessage();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player != null && player.isCommandAvailable(COMMAND_GET_CURRENT_MEDIA_ITEM) && player.isPlayingAd()) {
            return super.dispatchKeyEvent(event);
        }

        boolean isDpadKey = isDpadKey(event.getKeyCode());
        boolean handled = false;
        if (isDpadKey && useController() && !Objects.requireNonNull(controller).isFullyVisible()) {
            // Handle the key event by showing the controller.
            maybeShowController(true);
            handled = true;
        } else if (dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event)) {
            // The key event was handled as a media key or by the super class. We should also show the
            // controller, or extend its show timeout if already visible.
            maybeShowController(true);
            handled = true;
        } else if (isDpadKey && useController()) {
            // The key event wasn't handled, but we should extend the controller's show timeout.
            maybeShowController(true);
        }
        return handled;
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController() && Objects.requireNonNull(controller).dispatchMediaKeyEvent(event);
    }

    /**
     * Returns whether the controller is currently fully visible.
     */
    public boolean isControllerFullyVisible() {
        return controller != null && controller.isFullyVisible();
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     *
     * <p>The playback controls are automatically hidden during playback after {{@link
     * #getControllerShowTimeoutMs()}}. They are shown indefinitely when playback has not started yet,
     * is paused, has ended or failed.
     */
    public void showController() {
        showController(shouldShowControllerIndefinitely());
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause the
     *                                controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkStateNotNull(controller);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
        if (controller != null && Objects.requireNonNull(controller).isFullyVisible()) {
            // Update the controller's timeout if necessary.
            showController();
        }
    }

    /**
     * Returns whether the playback controls are hidden by touch events.
     */
    public boolean getControllerHideOnTouch() {
        return controllerHideOnTouch;
    }

    /**
     * Sets whether the playback controls are hidden by touch events.
     *
     * @param controllerHideOnTouch Whether the playback controls are hidden by touch events.
     */
    public void setControllerHideOnTouch(boolean controllerHideOnTouch) {
        Assertions.checkStateNotNull(controller);
        this.controllerHideOnTouch = controllerHideOnTouch;
        updateContentDescription();
    }

    /**
     * Returns whether the playback controls are automatically shown when playback starts, pauses,
     * ends, or fails. If set to false, the playback controls can be manually operated with {@link
     * #showController()} and {@link #hideController()}.
     */
    public boolean getControllerAutoShow() {
        return controllerAutoShow;
    }

    /**
     * Sets whether the playback controls are automatically shown when playback starts, pauses, ends,
     * or fails. If set to false, the playback controls can be manually operated with {@link
     * #showController()} and {@link #hideController()}.
     *
     * @param controllerAutoShow Whether the playback controls are allowed to show automatically.
     */
    public void setControllerAutoShow(boolean controllerAutoShow) {
        this.controllerAutoShow = controllerAutoShow;
    }

    /**
     * Sets whether the playback controls are hidden when ads are playing. Controls are always shown
     * during ads if they are enabled and the player is paused.
     *
     * @param controllerHideDuringAds Whether the playback controls are hidden when ads are playing.
     */
    public void setControllerHideDuringAds(boolean controllerHideDuringAds) {
        this.controllerHideDuringAds = controllerHideDuringAds;
    }

    /**
     * Sets the {@link StyledPlayerControlView.VisibilityListener}.
     *
     * <p>If {@code listener} is non-null then any listener set by {@link
     * #setControllerVisibilityListener(StyledPlayerControlView.VisibilityListener)} is removed.
     *
     * @param listener The listener to be notified about visibility changes, or null to remove the
     *                 current listener.
     */
    @SuppressWarnings("deprecation") // Clearing the legacy listener.
    public void setControllerVisibilityListener(@Nullable ControllerVisibilityListener listener) {
        this.controllerVisibilityListener = listener;
        if (listener != null) {
            setControllerVisibilityListener((StyledPlayerControlView.VisibilityListener) null);
        }
    }

    /**
     * Sets the {@link StyledPlayerControlView.VisibilityListener}.
     *
     * <p>If {@code listener} is non-null then any listener set by {@link
     * #setControllerVisibilityListener(ControllerVisibilityListener)} is removed.
     *
     * @deprecated Use {@link #setControllerVisibilityListener(ControllerVisibilityListener)} instead.
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public void setControllerVisibilityListener(@Nullable StyledPlayerControlView.VisibilityListener listener) {
        Assertions.checkStateNotNull(controller);
        if (this.legacyControllerVisibilityListener == listener) {
            return;
        }

        if (this.legacyControllerVisibilityListener != null) {
            if (controller != null) {
                controller.removeVisibilityListener(this.legacyControllerVisibilityListener);
            }
        }
        this.legacyControllerVisibilityListener = listener;
        if (listener != null) {
            if (controller != null) {
                controller.addVisibilityListener(listener);
            }
            setControllerVisibilityListener((ControllerVisibilityListener) null);
        }
    }

    /**
     * Sets the {@link FullscreenButtonClickListener}.
     *
     * <p>Clears any listener set by {@link
     * #setControllerOnFullScreenModeChangedListener(StyledPlayerControlView.OnFullScreenModeChangedListener)}.
     *
     * @param listener The listener to be notified when the fullscreen button is clicked, or null to
     *                 remove the current listener and hide the fullscreen button.
     */
    @SuppressWarnings("deprecation")
    // Calling the deprecated method on StyledPlayerControlView for now.
    public void setFullscreenButtonClickListener(@Nullable FullscreenButtonClickListener listener) {
        Assertions.checkStateNotNull(controller);
        this.fullscreenButtonClickListener = listener;
        if (controller != null) {
            controller.setOnFullScreenModeChangedListener(componentListener);
        }
    }

    /**
     * Sets the {@link StyledPlayerControlView.OnFullScreenModeChangedListener}.
     *
     * <p>Clears any listener set by {@link
     * #setFullscreenButtonClickListener(FullscreenButtonClickListener)}.
     *
     * @param listener The listener to be notified when the fullscreen button is clicked, or null to
     *                 remove the current listener and hide the fullscreen button.
     * @deprecated Use {@link #setFullscreenButtonClickListener(FullscreenButtonClickListener)}
     * instead.
     */
    @Deprecated
    public void setControllerOnFullScreenModeChangedListener(@Nullable StyledPlayerControlView.OnFullScreenModeChangedListener listener) {
        Assertions.checkStateNotNull(controller);
        this.fullscreenButtonClickListener = null;
        controller.setOnFullScreenModeChangedListener(listener);
    }

    /**
     * Sets whether the rewind button is shown.
     *
     * @param showRewindButton Whether the rewind button is shown.
     */
    public void setShowRewindButton(boolean showRewindButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowRewindButton(showRewindButton);
        }
    }

    /**
     * Sets whether the fast forward button is shown.
     *
     * @param showFastForwardButton Whether the fast forward button is shown.
     */
    public void setShowFastForwardButton(boolean showFastForwardButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowFastForwardButton(showFastForwardButton);
        }
    }

    /**
     * Sets whether the previous button is shown.
     *
     * @param showPreviousButton Whether the previous button is shown.
     */
    public void setShowPreviousButton(boolean showPreviousButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowPreviousButton(showPreviousButton);
        }
    }

    /**
     * Sets whether the next button is shown.
     *
     * @param showNextButton Whether the next button is shown.
     */
    public void setShowNextButton(boolean showNextButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowNextButton(showNextButton);
        }
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public void setRepeatToggleModes(@RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setRepeatToggleModes(repeatToggleModes);
        }
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    public void setShowShuffleButton(boolean showShuffleButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowShuffleButton(showShuffleButton);
        }
    }

    /**
     * Sets whether the subtitle button is shown.
     *
     * @param showSubtitleButton Whether the subtitle button is shown.
     */
    public void setShowSubtitleButton(boolean showSubtitleButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowSubtitleButton(showSubtitleButton);
        }
    }

    /**
     * Sets whether the vr button is shown.
     *
     * @param showVrButton Whether the vr button is shown.
     */
    public void setShowVrButton(boolean showVrButton) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowVrButton(showVrButton);
        }
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one.
     *
     * @param showMultiWindowTimeBar Whether to show all windows.
     */
    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setShowMultiWindowTimeBar(showMultiWindowTimeBar);
        }
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     *                            {@code null} to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played, or {@code null} to show no extra ad
     *                            markers.
     */
    public void setExtraAdGroupMarkers(@Nullable long[] extraAdGroupTimesMs, @Nullable boolean[] extraPlayedAdGroups) {
        Assertions.checkStateNotNull(controller);
        if (controller != null) {
            controller.setExtraAdGroupMarkers(extraAdGroupTimesMs, extraPlayedAdGroups);
        }
    }

    /**
     * Sets the {@link AspectRatioFrameLayout.AspectRatioListener}.
     *
     * @param listener The listener to be notified about aspect ratios changes of the video content or
     *                 the content frame.
     */
    public void setAspectRatioListener(@Nullable AspectRatioFrameLayout.AspectRatioListener listener) {
        Assertions.checkStateNotNull(contentFrame);
        if (contentFrame != null) {
            contentFrame.setAspectRatioListener(listener);
        }
    }

    /**
     * Gets the view onto which video is rendered. This is a:
     *
     * <ul>
     *   <li>{@link SurfaceView} by default, or if the {@code surface_type} attribute is set to {@code
     *       surface_view}.
     *   <li>{@link TextureView} if {@code surface_type} is {@code texture_view}.
     *   <li>{@code SphericalGLSurfaceView} if {@code surface_type} is {@code
     *       spherical_gl_surface_view}.
     *   <li>{@code VideoDecoderGLSurfaceView} if {@code surface_type} is {@code
     *       video_decoder_gl_surface_view}.
     *   <li>{@code null} if {@code surface_type} is {@code none}.
     * </ul>
     *
     * @return The {@link SurfaceView}, {@link TextureView}, {@code SphericalGLSurfaceView}, {@code
     * VideoDecoderGLSurfaceView} or {@code null}.
     */
    @Nullable
    public View getVideoSurfaceView() {
        return surfaceView;
    }

    /**
     * Gets the overlay {@link FrameLayout}, which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay {@link FrameLayout}, or {@code null} if the layout has been customized and
     * the overlay is not present.
     */
    @Nullable
    public FrameLayout getOverlayFrameLayout() {
        return overlayFrameLayout;
    }

    /**
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     * subtitle view is not present.
     */
    @Nullable
    public SubtitleView getSubtitleView() {
        return subtitleView;
    }

    @Override
    public boolean performClick() {
        toggleControllerVisibility();
        return super.performClick();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController() || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    /**
     * Should be called when the player is visible to the user, if the {@code surface_type} extends
     * {@link GLSurfaceView}. It is the counterpart to {@link #onPause()}.
     *
     * <p>This method should typically be called in {@code Activity.onStart()}, or {@code
     * Activity.onResume()} for API versions &lt;= 23.
     */
    public void onResume() {
        if (surfaceView instanceof GLSurfaceView) {
            ((GLSurfaceView) surfaceView).onResume();
        }
    }

    /**
     * Should be called when the player is no longer visible to the user, if the {@code surface_type}
     * extends {@link GLSurfaceView}. It is the counterpart to {@link #onResume()}.
     *
     * <p>This method should typically be called in {@code Activity.onStop()}, or {@code
     * Activity.onPause()} for API versions &lt;= 23.
     */
    public void onPause() {
        if (surfaceView instanceof GLSurfaceView) {
            ((GLSurfaceView) surfaceView).onPause();
        }
    }

    /**
     * Called when there's a change in the desired aspect ratio of the content frame. The default
     * implementation sets the aspect ratio of the content frame to the specified value.
     *
     * @param contentFrame The content frame, or {@code null}.
     * @param aspectRatio  The aspect ratio to apply.
     */
    protected void onContentAspectRatioChanged(@Nullable AspectRatioFrameLayout contentFrame, float aspectRatio) {
        if (contentFrame != null) {
            contentFrame.setAspectRatio(aspectRatio);
        }
    }

    // AdsLoader.AdViewProvider implementation.

    @Override
    public ViewGroup getAdViewGroup() {
        return Assertions.checkStateNotNull(adOverlayFrameLayout, "exo_ad_overlay must be present for ad playback");
    }

    @NonNull
    @Override
    public List<AdOverlayInfo> getAdOverlayInfos() {
        List<AdOverlayInfo> overlayViews = new ArrayList<>();
        if (overlayFrameLayout != null) {
            overlayViews.add(new AdOverlayInfo.Builder(overlayFrameLayout, AdOverlayInfo.PURPOSE_NOT_VISIBLE).setDetailedReason("Transparent overlay does not impact viewability").build());
        }
        if (controller != null) {
            overlayViews.add(new AdOverlayInfo.Builder(controller, AdOverlayInfo.PURPOSE_CONTROLS).build());
        }
        return ImmutableList.copyOf(overlayViews);
    }

    // Internal methods.


    private boolean useController() {
        if (useController) {
            Assertions.checkStateNotNull(controller);
            return true;
        }
        return false;
    }


    private boolean useArtwork() {
        if (useArtwork) {
            Assertions.checkStateNotNull(artworkView);
            return true;
        }
        return false;
    }

    private void toggleControllerVisibility() {
        if (!useController() || player == null) {
            return;
        }
        if (controller != null) {
            if (!controller.isFullyVisible()) {
                maybeShowController(true);
            } else if (controllerHideOnTouch) {
                controller.hide();
            }
        }
    }

    /**
     * Shows the playback controls, but only if forced or shown indefinitely.
     */
    private void maybeShowController(boolean isForced) {
        if (isPlayingAd() && controllerHideDuringAds) {
            return;
        }
        if (useController()) {
            boolean wasShowingIndefinitely = false;
            if (controller != null) {
                wasShowingIndefinitely = controller.isFullyVisible() && controller.getShowTimeoutMs() <= 0;
            }
            boolean shouldShowIndefinitely = shouldShowControllerIndefinitely();
            if (isForced || wasShowingIndefinitely || shouldShowIndefinitely) {
                showController(shouldShowIndefinitely);
            }
        }
    }

    private boolean shouldShowControllerIndefinitely() {
        if (player == null) {
            return true;
        }
        int playbackState = player.getPlaybackState();
        return controllerAutoShow && (!player.isCommandAvailable(COMMAND_GET_TIMELINE) || !player.getCurrentTimeline().isEmpty()) && (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !checkNotNull(player).getPlayWhenReady());
    }

    private void showController(boolean showIndefinitely) {
        if (!useController()) {
            return;
        }
        if (controller != null) {
            controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
            controller.show();
        }
    }

    private boolean isPlayingAd() {
        return player != null && player.isCommandAvailable(COMMAND_GET_CURRENT_MEDIA_ITEM) && player.isPlayingAd() && player.getPlayWhenReady();
    }

    private void updateForCurrentTrackSelections(boolean isNewPlayer) {
        @Nullable Player player = this.player;
        if (player == null || !player.isCommandAvailable(COMMAND_GET_TRACKS) || player.getCurrentTracks().isEmpty()) {
            if (!keepContentOnPlayerReset) {
                hideArtwork();
                closeShutter();
            }
            return;
        }

        if (isNewPlayer && !keepContentOnPlayerReset) {
            // Hide any video from the previous player.
            closeShutter();
        }

        if (player.getCurrentTracks().isTypeSelected(C.TRACK_TYPE_VIDEO)) {
            // Video enabled, so artwork must be hidden. If the shutter is closed, it will be opened
            // in onRenderedFirstFrame().
            hideArtwork();
            return;
        }

        // Video disabled so the shutter must be closed.
        closeShutter();
        // Display artwork if enabled and available, else hide it.
        if (useArtwork()) {
            if (setArtworkFromMediaMetadata(player)) {
                return;
            }
            if (setDrawableArtwork(defaultArtwork)) {
                return;
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }


    private boolean setArtworkFromMediaMetadata(Player player) {
        if (!player.isCommandAvailable(COMMAND_GET_MEDIA_ITEMS_METADATA)) {
            return false;
        }
        MediaMetadata mediaMetadata = player.getMediaMetadata();
        if (mediaMetadata.artworkData == null) {
            return false;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(mediaMetadata.artworkData, /* offset= */ 0, mediaMetadata.artworkData.length);
        return setDrawableArtwork(new BitmapDrawable(getResources(), bitmap));
    }


    private boolean setDrawableArtwork(@Nullable Drawable drawable) {
        if (drawable != null) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            if (drawableWidth > 0 && drawableHeight > 0) {
                float artworkAspectRatio = (float) drawableWidth / drawableHeight;
                onContentAspectRatioChanged(contentFrame, artworkAspectRatio);
                if (artworkView != null) {
                    artworkView.setImageDrawable(drawable);
                    artworkView.setVisibility(VISIBLE);
                }
                return true;
            }
        }
        return false;
    }

    private void hideArtwork() {
        if (artworkView != null) {
            artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
            artworkView.setVisibility(INVISIBLE);
        }
    }

    private void closeShutter() {
        if (shutterView != null) {
            shutterView.setVisibility(View.VISIBLE);
        }
    }

    private void updateBuffering() {
        if (bufferingView != null) {
            boolean showBufferingSpinner = player != null && player.getPlaybackState() == Player.STATE_BUFFERING && (showBuffering == SHOW_BUFFERING_ALWAYS || (showBuffering == SHOW_BUFFERING_WHEN_PLAYING && player.getPlayWhenReady()));
            bufferingView.setVisibility(showBufferingSpinner ? View.VISIBLE : View.GONE);
        }
    }

    private void updateErrorMessage() {
        if (errorMessageView != null) {
            if (customErrorMessage != null) {
                errorMessageView.setText(customErrorMessage);
                errorMessageView.setVisibility(View.VISIBLE);
                return;
            }
            @Nullable PlaybackException error = player != null ? player.getPlayerError() : null;
            if (error != null && errorMessageProvider != null) {
                CharSequence errorMessage = errorMessageProvider.getErrorMessage(error).second;
                errorMessageView.setText(errorMessage);
                errorMessageView.setVisibility(View.VISIBLE);
            } else {
                errorMessageView.setVisibility(View.GONE);
            }
        }
    }

    private void updateContentDescription() {
        if (controller == null || !useController) {
            setContentDescription(/* contentDescription= */ null);
        } else if (controller.isFullyVisible()) {
            setContentDescription(
                    /* contentDescription= */ controllerHideOnTouch ? getResources().getString(R.string.exo_controls_hide) : null);
        } else {
            setContentDescription(
                    /* contentDescription= */ getResources().getString(R.string.exo_controls_show));
        }
    }

    private void updateControllerVisibility() {
        if (isPlayingAd() && controllerHideDuringAds) {
            hideController();
        } else {
            maybeShowController(false);
        }
    }

    private void updateAspectRatio() {
        VideoSize videoSize = player != null ? player.getVideoSize() : VideoSize.UNKNOWN;
        int width = videoSize.width;
        int height = videoSize.height;
        int unappliedRotationDegrees = videoSize.unappliedRotationDegrees;
        float videoAspectRatio = (height == 0 || width == 0) ? 0 : (width * videoSize.pixelWidthHeightRatio) / height;

        if (surfaceView instanceof TextureView) {
            // Try to apply rotation transformation when our surface is a TextureView.
            if (videoAspectRatio > 0 && (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270)) {
                // We will apply a rotation 90/270 degree to the output texture of the TextureView.
                // In this case, the output video's width and height will be swapped.
                videoAspectRatio = 1 / videoAspectRatio;
            }
            if (textureViewRotation != 0) {
                surfaceView.removeOnLayoutChangeListener(componentListener);
            }
            textureViewRotation = unappliedRotationDegrees;
            if (textureViewRotation != 0) {
                // The texture view's dimensions might be changed after layout step.
                // So add an OnLayoutChangeListener to apply rotation after layout step.
                surfaceView.addOnLayoutChangeListener(componentListener);
            }
            applyTextureViewRotation((TextureView) surfaceView, textureViewRotation);
        }

        onContentAspectRatioChanged(contentFrame, surfaceViewIgnoresVideoAspectRatio ? 0 : videoAspectRatio);
    }


    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    /**
     * Applies a texture rotation to a {@link TextureView}.
     */
    private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
        Matrix transformMatrix = new Matrix();
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        if (textureViewWidth != 0 && textureViewHeight != 0 && textureViewRotation != 0) {
            float pivotX = textureViewWidth / 2;
            float pivotY = textureViewHeight / 2;
            transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);

            // After rotation, scale the rotated texture to fit the TextureView size.
            RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
            RectF rotatedTextureRect = new RectF();
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
            transformMatrix.postScale(textureViewWidth / rotatedTextureRect.width(), textureViewHeight / rotatedTextureRect.height(), pivotX, pivotY);
        }
        textureView.setTransform(transformMatrix);
    }

    @SuppressLint("InlinedApi")
    private boolean isDpadKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_CENTER;
    }

    // Implementing the deprecated StyledPlayerControlView.VisibilityListener and
    // StyledPlayerControlView.OnFullScreenModeChangedListener for now.
    @SuppressWarnings("deprecation")
    private final class ComponentListener implements Player.Listener, OnLayoutChangeListener, OnClickListener, StyledPlayerControlView.VisibilityListener, StyledPlayerControlView.OnFullScreenModeChangedListener {

        private final Period period;
        private @Nullable
        Object lastPeriodUidWithTracks;

        public ComponentListener() {
            period = new Period();
        }

        // Player.Listener implementation

        @Override
        public void onCues(@NonNull CueGroup cueGroup) {
            if (subtitleView != null) {
                subtitleView.setCues(cueGroup.cues);
            }
        }

        @Override
        public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
            updateAspectRatio();
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onTracksChanged(@NonNull Tracks tracks) {
            // Suppress the update if transitioning to an unprepared period within the same window. This
            // is necessary to avoid closing the shutter when such a transition occurs. See:
            // https://github.com/google/ExoPlayer/issues/5507.
            Player player = checkNotNull(ExoVideoView.this.player);
            Timeline timeline = player.isCommandAvailable(COMMAND_GET_TIMELINE) ? player.getCurrentTimeline() : Timeline.EMPTY;
            if (timeline.isEmpty()) {
                lastPeriodUidWithTracks = null;
            } else if (player.isCommandAvailable(COMMAND_GET_TRACKS) && !player.getCurrentTracks().isEmpty()) {
                lastPeriodUidWithTracks = timeline.getPeriod(player.getCurrentPeriodIndex(), period, /* setIds= */ true).uid;
            } else if (lastPeriodUidWithTracks != null) {
                int lastPeriodIndexWithTracks = timeline.getIndexOfPeriod(lastPeriodUidWithTracks);
                if (lastPeriodIndexWithTracks != C.INDEX_UNSET) {
                    int lastWindowIndexWithTracks = timeline.getPeriod(lastPeriodIndexWithTracks, period).windowIndex;
                    if (player.getCurrentMediaItemIndex() == lastWindowIndexWithTracks) {
                        // We're in the same media item. Suppress the update.
                        return;
                    }
                }
                lastPeriodUidWithTracks = null;
            }

            updateForCurrentTrackSelections(/* isNewPlayer= */ false);
        }

        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            updateBuffering();
            updateErrorMessage();
            updateControllerVisibility();
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
            updateBuffering();
            updateControllerVisibility();
        }

        @Override
        public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, @DiscontinuityReason int reason) {
            if (isPlayingAd() && controllerHideDuringAds) {
                hideController();
            }
        }

        // OnLayoutChangeListener implementation

        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            applyTextureViewRotation((TextureView) view, textureViewRotation);
        }

        // OnClickListener implementation

        @Override
        public void onClick(View view) {
            toggleControllerVisibility();
        }

        // StyledPlayerControlView.VisibilityListener implementation
        @Override
        public void onVisibilityChange(int visibility) {
            updateContentDescription();
            if (controllerVisibilityListener != null) {
                controllerVisibilityListener.onVisibilityChanged(visibility);
            }
        }

        // StyledPlayerControlView.OnFullScreenModeChangedListener implementation

        @Override
        public void onFullScreenModeChanged(boolean isFullScreen) {
            if (fullscreenButtonClickListener != null) {
                fullscreenButtonClickListener.onFullscreenButtonClick(isFullScreen);
            }
        }
    }
}

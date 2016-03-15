package com.mbh.mediaplayertutorial;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.RawRes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by MBH on 13/03/16.
 */
public class MBMediaPlayer {

    private MBLogger logger; // Logger used for logging errors and debug notes
    private final static int seekForwardTime = 5000; // 5 seconds
    private final static int seekBackwardTime = 5000; // 5 seconds
    private int currentSoundIndex = 0; // will track the current player sound
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isPlayAll = false;
    private boolean isPaused = false;
    private boolean isRepeatAll = false;

    private ArrayList<MediaFile> soundsList;

    private MediaPlayer mp;

    private Context mContext = null;

    private OnCompletionListener onMyComplestionListener = null;

    public MBMediaPlayer(Context context) {
        mp = new MediaPlayer();
        logger = new MBLogger.Builder().setTag(this).createLogger();
        mp.setOnCompletionListener(complitionListener);
        soundsList = new ArrayList<>();
        mContext = context;
    }


    public boolean isRepeatAll() {
        return isRepeatAll;
    }
    public void repeatAll(boolean isRepeatAll) {
        this.isRepeatAll = isRepeatAll;
    }

    public boolean isRepeatOne() {
        return isRepeat;
    }
    public void repeatOne(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public boolean isShuffleOn() {
        return isShuffle;
    }
    public void shuffle(boolean isShuffle) {
        this.isShuffle = isShuffle;
    }

    public void setPlayAll(boolean isPlayAll) {
        this.isPlayAll = isPlayAll;
    }

    public void playAllNow() {
        setPlayAll(true);
        playSoundAt(0);
    }

    public void setOnComplestionListener(OnCompletionListener onMyComplestionListener){
        this.onMyComplestionListener = onMyComplestionListener;
    }

    public void addRawFile(@RawRes int rawRes) {
        addRawFile("", rawRes);
    }

    public void addRawFile(String soundTitle, @RawRes int rawRes) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaTitle(soundTitle);
        mediaFile.setRaw(rawRes);
        mediaFile.setMediaPath(rawRes + "");
        soundsList.add(mediaFile);
    }

    public void addAssetFile(String soundTitle, String fileName) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaTitle(soundTitle);
        mediaFile.setAsset(fileName);
        soundsList.add(mediaFile);
    }

    public void addAssetFile( String fileName) {
        addAssetFile("", fileName);
    }

    public void addFile(String songPath) {
        addFile("", songPath);
    }

    public void addFile(String soundTitle, String songPath) {
        MediaFile mf = new MediaFile(soundTitle, songPath);
        soundsList.add(mf);
    }

    public void playSoundAt(int soundIndex) {

        logger.debug("Playing file at index = "+soundIndex);
        try {
            if(mp == null)
                mp = new MediaPlayer();
            mp.reset();
            MediaFile mediaFile = soundsList.get(soundIndex);
            if(mediaFile.isAsset()){
                AssetFileDescriptor afd = mContext.getAssets().openFd(mediaFile.getAssetFileName());
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.prepare();
            }else if (mediaFile.isRaw()){
                mp = MediaPlayer.create(mContext, mediaFile.getRawId());
                mp.setOnCompletionListener(complitionListener);
            }else {
                mp.setDataSource(soundsList.get(soundIndex).getMediaPath());
                mp.prepare();
            }

            mp.start();
            isPaused = false;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public boolean isPlaying() {
        if (mp != null && mp.isPlaying())
            return true;
        return false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void pause() {
        if (mp == null)
            mp = new MediaPlayer();
        if (mp.isPlaying()) {
            mp.pause();
            isPaused = true;
        }
    }

    public boolean playOrPause(boolean isPlayAll) {
        this.isPlayAll = isPlayAll;

        // check for already playing
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            // Paused
            isPaused = true;
            return false;

        } else {
            if (mp == null) {
                playSoundAt(currentSoundIndex);
            }else
                mp.start();
            isPaused = false;
            return true;
        }
    }

    public void moveForward() {
        if(mp == null) mp = new MediaPlayer();
        int currentPosition = mp.getCurrentPosition();
        if (currentPosition + seekForwardTime <= mp.getDuration()) {
            mp.seekTo(currentPosition + seekForwardTime);
        } else {
            mp.seekTo(mp.getDuration());
        }
    }

    public void next() {
        // get current song position
        if(mp == null) mp = new MediaPlayer();
        // check if next song is there or not
        if(currentSoundIndex < (soundsList.size() - 1)){
            playSoundAt(currentSoundIndex + 1);
            currentSoundIndex = currentSoundIndex + 1;
        }else{
            // play first song
            playSoundAt(0);
            currentSoundIndex = 0;
        }
    }

    public void previous() {
        if(mp == null) mp = new MediaPlayer();
        if(currentSoundIndex > 0){
            playSoundAt(currentSoundIndex - 1);
            currentSoundIndex = currentSoundIndex - 1;
        }else{
            // play last song
            playSoundAt(soundsList.size() - 1);
            currentSoundIndex = soundsList.size() - 1;
        }
    }

    public void moveBackward() {
        if(mp == null) mp = new MediaPlayer();
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0) {
            // forward song
            mp.seekTo(currentPosition - seekBackwardTime);
        } else {
            // backward to starting position
            mp.seekTo(0);
        }
    }

    public void destroy() {
        if (mp != null)
            mp.release();
        mp = null;
    }

    private MediaPlayer.OnCompletionListener complitionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isRepeat) {
                logger.debug("Repeating the same sound file = "+currentSoundIndex);
                playSoundAt(currentSoundIndex);
            } else if (isShuffle) {
                Random rand = new Random();
                currentSoundIndex = rand.nextInt((soundsList.size() - 1) + 1);
                logger.debug("Playing Random sound file = "+currentSoundIndex);
                playSoundAt(currentSoundIndex);
            } else {
                if (!isPlayAll) {
                    if (onMyComplestionListener != null) {
                        onMyComplestionListener.onCompletion();
                    }
                    return;
                }
                if (currentSoundIndex < (soundsList.size() - 1)) {
                    playSoundAt(currentSoundIndex + 1);
                    currentSoundIndex = currentSoundIndex + 1;
                    logger.debug("Playing next Sound file = "+currentSoundIndex);
                } else {
                    if(isRepeatAll) {
                        currentSoundIndex = 0;
                        playSoundAt(0);
                        logger.debug("Finished all and repeating all = "+currentSoundIndex);
                    }else {
                        if(onMyComplestionListener != null){
                            onMyComplestionListener.onCompletion();
                            logger.debug("Finished all and no repeat = "+currentSoundIndex);
                        }
                    }
                }
            }
        }
    };

    interface OnCompletionListener {
        void onCompletion();
    }

    public class MediaFile {
        private String mediaPath;
        private String mediaTitle;
        private int rawId = -1;
        private String assetFileName = null;

        public MediaFile(String title, String path) {
            mediaTitle = title;
            mediaPath = path;
        }


        public void setRaw(@RawRes int rawRes) {
            rawId = rawRes;
        }

        public @RawRes int getRawId(){
            return rawId;
        }

        public String getAssetFileName(){
            return assetFileName;
        }

        public void setAsset(String assetFileName) {
            this.assetFileName = assetFileName;
        }

        public boolean isAsset() {
            return assetFileName != null;
        }

        public boolean isRaw() {
            return rawId != -1;
        }

        public MediaFile() {
            mediaPath = "";
            mediaTitle = "";
        }

        @Override
        public String toString() {
            return "MediaTitle=" + mediaTitle + ", MediaPath=" + mediaPath;
        }

        public String getMediaPath() {
            return mediaPath;
        }

        public void setMediaPath(String mediaPath) {
            this.mediaPath = mediaPath;
        }

        public String getMediaTitle() {
            return mediaTitle;
        }

        public void setMediaTitle(String mediaTitle) {
            this.mediaTitle = mediaTitle;
        }
    }
}

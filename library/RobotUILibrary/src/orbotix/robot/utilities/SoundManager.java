package orbotix.robot.utilities;

/**
 * Created by Orbotix Inc.
 * User: brandon
 * Date: 10/24/11
 * Time: 3:06 PM
 */
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseIntArray;

/**
 * A convenience class that allows sounds to be loaded once in your application and then accessed from any activity.
 * Sounds can also be loaded as they are needed to improve performance. This is meant to be used as an easy way to add
 * sounds to your application or game. If you need more control over the sound in your app, we recommend that you use
 * the {@link SoundPool} class and implement your own manager.
 *
 * To start, call {@link #initialize(android.content.Context)} to do the initial setup and to allow the SoundManager to
 * begin accepting {@link #addSound(Context, int, int)} and {@link #playSound(int)} calls. The SoundManager currently only
 * supports sound files from resources (e.g. R.raw.sound_file) or files with known paths
 * (e.g. /sdcard/cache/app/sound_file.ext). Use the addSound methods to add your sounds to the SoundManager ahead of
 * time (best performance) or just before they are needed (worse performance). When you are ready to play a sound,
 * simply call {@link #playSound(int)} using the soundIdentifier you passed in with the sound you wish to play. If,
 * for some reason, you need to unload all of your sounds, call
 */
public class SoundManager {

    public static final int REPEAT_FOREVER = 0;

    private static SoundManager sInstance = new SoundManager();

    private SoundPool mSoundPool;
    private SparseIntArray mSoundMap;
    private AudioManager mAudioManager;
    private float mVolumeMultiplier = 0.0f;

    private MediaPlayer mMediaPlayer;

    /**
     * Call this when your app exits to make sure everything is torn down correctly
     */
    public static void tearDown() {
        if (sInstance.mSoundPool != null) {
            sInstance.mSoundPool.release();
            sInstance.mSoundPool = null;
        }
        if (sInstance.mSoundMap != null) {
            sInstance.mSoundMap = null;
        }
        if (sInstance.mAudioManager != null) {
            sInstance.mAudioManager = null;
        }
    }

    /**
     * Initializes the SoundManager and allows it to load and play sound files. This should be called first thing
     * before any adds or plays.
     * @param context the context from your application
     */
    public static void initialize(Context context) {
        //Log.e("Orbotix", "initialized");
        sInstance.mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        sInstance.mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        sInstance.mSoundMap = new SparseIntArray();
    }

    /**
     * Adds a sound to the sound manager for future playback.
     * @param soundIdentifier the identifier for this sound to be used for future playback. This will be the id
     * used to play the sound using {@link #playSound(int)}.
     * @param resourceId the resource id of the sound file itself. (e.g. R.raw.swoosh_sound)
     */
    public static void addSound(Context context, int soundIdentifier, int resourceId) {
        sInstance.mSoundMap.append(soundIdentifier, sInstance.mSoundPool.load(context, resourceId, 1));
    }

    /**
     * Adds a sound to the sound manager for future playback.
     * @param soundIdentifier the identifier for this sound to be used for future playback. This will be the id
     * used to play the sound using {@link #playSound(int)}.
     * @param path the specific path to the sound file to be used.
     */
    public static void addSound(int soundIdentifier, String path) {
        sInstance.mSoundMap.append(soundIdentifier, sInstance.mSoundPool.load(path, 1));
    }

    /**
     * Attempts to play the sound identified by soundIdentifier (set by {@link #addSound(Context, int, int)}) once.
     * @param soundIdentifier the identifier for the specific sound you are wanting to play (set by
     * {@link #addSound(Context, int, int)}).
     * @return The stream id that the sound has been assigned. Use this integer to later stop the sound if you need
     * to, using {@link #stopSound(int)}
     * @throws SoundManagerNotConfiguredException if the SoundManager has not been configured correctly. This is most
     * likely caused by not calling {@link #initialize(android.content.Context)} before trying to play a sound.
     *
     */
    public static int playSound(int soundIdentifier) {
        return playSound(soundIdentifier, 1);
    }

    /**
     * Attempts to play the sound identified by soundIdentifier (set by {@link #addSound(Context, int, int)}) the number of times
     * specified by numberOfPlays.
     * @param soundIdentifier the identifier for the specific sound you are wanting to play (set by
     * {@link #addSound(Context, int, int)}).
     * @param numberOfPlays the number of times you want the sound to play (e.g. 3 will play the sound 3 times). Use
     * {@link SoundManager#REPEAT_FOREVER} if you want the sound to continually repeat.
     * @return The Stream ID that the sound has been assigned
     */
    public static int playSound(int soundIdentifier, int numberOfPlays) {
        if (sInstance.mSoundPool == null) {
            return -1;
        }
        float volume = (float)sInstance.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) /
                (float)sInstance.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) *
                sInstance.mVolumeMultiplier;
        return sInstance.mSoundPool.play(sInstance.mSoundMap.get(soundIdentifier), volume, volume, 1, numberOfPlays - 1, 1.0f);
    }

    /**
     * Attempts to play a larger sound file using Android's MediaPlayer. Use this if {@link #playSound(int, int)} is
     * unable to play the file because of its size or duration.
     *
     * @param context The Android Context
     * @param res_id The integer ID of the resource file to play
     * @param loop If set to true, this sound file will play until stopped using {@link #stopMusic()}
     * @see #stopMusic()
     */
    public static void playMusic(Context context, int res_id, boolean loop){
        stopMusic();

        float volume = (float)sInstance.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) /
                (float)sInstance.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) *
                sInstance.mVolumeMultiplier;

        sInstance.mMediaPlayer = MediaPlayer.create(context, res_id);
        sInstance.mMediaPlayer.setLooping(loop);
        sInstance.mMediaPlayer.setVolume(volume, volume);
        sInstance.mMediaPlayer.start();
    }

    /**
     * Stops any music that may have been started in {@link #playMusic(android.content.Context, int, boolean)}
     *
     * @see #playMusic(android.content.Context, int, boolean)
     */
    public static void stopMusic(){
        if(sInstance.mMediaPlayer != null){
            if(sInstance.mMediaPlayer.isPlaying()){
                sInstance.mMediaPlayer.stop();
            }

            sInstance.mMediaPlayer.release();
        }

        sInstance.mMediaPlayer = null;
    }

    /**
     * Stops the specified sound if it is currently playing and it exists in the SoundManager.
     * @param streamIdentifier the stream ID for the sound to be stopped. This is the integer returned from
     * {@link #playSound(int, int)}
     */
    public static void stopSound(int streamIdentifier) {
        sInstance.mSoundPool.stop(streamIdentifier);
    }

    /**
     * Pauses any currently running sounds.
     */
    public static void pauseAllSound() {
        sInstance.mSoundPool.autoPause();
    }

    /**
     * Resumes any sounds that were paused using {@link #pauseAllSound()}
     */
    public static void resumeAllSound() {
        sInstance.mSoundPool.autoResume();
    }

    /**
     * Clears out all of the previously loaded sounds leaving an empty SoundManager. This is useful if you notice that
     * your application is using too much memory and you need to free up some resources to continue running your app.
     */
    public static void clearAllSounds() {
        sInstance.mSoundPool.release();
        sInstance = new SoundManager();
    }

    /**
     * Sets the volume [0,1] where 0 is muted and 1.0 is equal to the user's current media volume setting. Values
     * greater than 1.0 will result in maximum volume (current volume setting) while values less than 0.0 will result
     * in muted sound.
     * @param volume a value between 0 and 1 that represents the volume with respect to the current media volume setting
     */
    public static void setVolume(float volume) {
        if (volume < 0.0) {
            sInstance.mVolumeMultiplier = 0.0f;
        } else if (volume > 1.0) {
            sInstance.mVolumeMultiplier = 1.0f;
        } else {
            sInstance.mVolumeMultiplier = volume;
        }
    }

    /**
     * An exception indicating the {@link SoundManager} was not configured correctly and needed to be in order to
     * perform the operation that caused the exception.
     */
    public class SoundManagerNotConfiguredException extends Exception {
        public SoundManagerNotConfiguredException(String message) {
            super(message);
        }
    }
}

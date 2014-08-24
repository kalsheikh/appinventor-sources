// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.Display;
import com.google.appinventor.components.runtime.Player;

/**
 * Helper methods for calling methods added in Froyo (2.2, API level 8).
 *
 */
public class FroyoUtil {

  private FroyoUtil() {
  }

  /**
   * Calls {@link Display#getRotation()}
   *
   * @return one of {@link android.view.Surface#ROTATION_0},
   *         {@link android.view.Surface#ROTATION_90},
   *         {@link android.view.Surface#ROTATION_180},
   *         or {@link android.view.Surface#ROTATION_180}.
   */
  public static int getRotation(Display display) {
    return display.getRotation();
  }

  // Methods for Player Component
  /**
   * Utility method that returns and AudioManager for the Activity passed in
   * @param activity the Activity that will be associated to the AudioManager
   * @return the AudioManager object for the passed Activity
   */
  public static AudioManager setAudioManager(Activity activity) {
    return (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
  }

  /**
   * Utility method to generate a listener for changed on Focus. Only available in API Level 8
   * @param player the player which focus will be watched for
   * @return a listener object with associated callbacks for each state.
   */
  public static Object setAudioFocusChangeListener(final Player player) {
    Object afChangeListener = (android.media.AudioManager.OnAudioFocusChangeListener) new android
        .media.AudioManager.OnAudioFocusChangeListener() {
      private boolean playbackFlag = false;
      /**
       * This callback method is triggered when audio focus changes. This is necessary because
       * several apps can be managing audio at the same time and for them to interact correctly,
       * they should be using an AudioManager: http://developer.android.com/training/managing-audio/audio-focus.html
       * @param focusChange the type of focus change
       */
      @Override
      public void onAudioFocusChange(int focusChange) {
        switch(focusChange){
          case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
          case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Focus loss transient: Pause playback
            if (player != null && player.playerState == 2) {
              player.pause();
              playbackFlag = true;
            }
            break;
          case AudioManager.AUDIOFOCUS_LOSS:
            // Focus loss permanent: focus taken by other players
            playbackFlag = false;
            player.OtherPlayerStarted();
            break;
          case AudioManager.AUDIOFOCUS_GAIN:
            // Focus gain: Resume playback
            if (player != null && playbackFlag && player.playerState == 4) {
              player.Start();
              playbackFlag = false;
            }
            break;
        }
      }
    };
    return afChangeListener;
  }

  /**
   * Utility method that
   * @param am the AudioManager requesting the focus
   * @param afChangeListener the foucs listener associated to the AudioManager
   * @return true if focus is granted, false if not.
   */
  public static boolean focusRequestGranted(AudioManager am, Object afChangeListener) {
    int result = am.requestAudioFocus((AudioManager.OnAudioFocusChangeListener) afChangeListener,
        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  /**
   * Utility method to abandon audio focus given an AudioManager object
   * @param am the AudioManager object
   * @param afChangeListener the foucs listener associated to the AudioManager
   */
  public static void abandonFocus(AudioManager am, Object afChangeListener) {
    am.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener) afChangeListener);
  }
}

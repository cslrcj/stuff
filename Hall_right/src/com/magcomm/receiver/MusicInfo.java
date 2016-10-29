package com.magcomm.receiver;

public class MusicInfo
{

	private static boolean isMusic;
	private static boolean playing;
	private static String artistName="音乐家名字";
	private static String musicName="歌曲名字";
	public static boolean isMusic() 
	{
		return isMusic;
	}
	public static void setMusic(boolean isMusic)
	{
		MusicInfo.isMusic = isMusic;
	}
	public static boolean isPlaying() 
	{
		return playing;
	}
	public static void setPlaying(boolean playing) 
	{
		MusicInfo.playing = playing;
	}
	public static String getArtistName()
	{
		return artistName;
	}
	public static void setArtistName(String artistName) 
	{
		if ((artistName ==null) || (artistName.equals("")))
		{
			return;
		}
		MusicInfo.artistName = artistName;
	}
	public static String getMusicName() 
	{
		
		return musicName;
	}
	public static void setMusicName(String musicName) 
	{
		if ((musicName ==null) || (musicName.equals("")))
		{
			return;
		}
		MusicInfo.musicName = musicName;
	}
	
	
}

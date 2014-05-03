/**
 * @author Josh Bacon
 * @version V1.0
 * @date Winter break
 * Description: This program is a music organizer that sorts and organizes a music library folder's mp3 files into appropriate directories.
 * Key Features:
 * 			--Uses JAudioTagger (Use to be MP3AGIC) Library for TAG editing
 * 			--Multiple Threaded: Uses one Executer, a number of threads equal to number of processors, and a variable number of Runnable worker.
 * 			--Uses semaphore for keeping count of number of worker tasks
 * 			--Includes a good chunk of error checking with System.out.println
 * 
 * 
 * 
 * **Poor Modularity/Design Notice: This is not the most modular of programs, I developed it off/on over a duration for learning purposes for java and refreshing my knowledge before entering mobile computing.
 * What I should have done was make the relationship of the MusicOrganizer class communicate better with the MusicOrganizerMain class by using setters and getters accordingly instead of public static instance variables.
 * But going into this project I really did not have a definite plan and only the simple purpose of organizing my personal music library initially.

 */

import java.lang.ThreadGroup;
import java.beans.Encoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//import com.mpatric.mp3agic.EncodedText;
//import com.mpatric.mp3agic.ID3v23Tag;
//import com.mpatric.mp3agic.ID3v24Tag;
//import com.mpatric.mp3agic.InvalidDataException;
//import com.mpatric.mp3agic.Mp3File;
//import com.mpatric.mp3agic.UnsupportedTagException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.reference.GenreTypes;

public class MusicOrganizer implements Runnable{
	private Path rootMusicFolderPath;
	private Path musicDir;
	private DirectoryStream<Path> pathsStream;

	public void run() {
		//Start organizer
		try {
			OrganizeMusicDirectory();
			MusicOrganizerMain.sem.acquire();
			MusicOrganizerMain.numTasks--;
			MusicOrganizerMain.sem.release();
			//if(MusicOrganizerMain.numTasks == 0) {
			//	Thread.currentThread().notify();
			//}
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}
	}
	
	/**
	 * Description: Starts organizing the music directory
	 * 
	 * @param musicDirPath The path of the directory to organize
	 */
	public MusicOrganizer(Path musicDirPath) {
		this.musicDir = musicDirPath;
		this.rootMusicFolderPath = MusicOrganizerMain.rootMusicPath;
		try {
			pathsStream = Files.newDirectoryStream(musicDir);
		} catch (IOException  e) {
			e.printStackTrace();
			System.out.println("ERROR: Could not initialize DirectoryStream<Path> pathsStream in contructor");
			System.exit(0);
		}
	}
	
	/**
	 * Description: Will edit the songs tag to make all songs have uniform TAG versions (ID3v2.4), and will also delete the old tags
	 * This way all songs will be uniform in Tag and music players will 
	 * In the past used M3PAGIC Library, but it had too many bugs or strange behaviors
	 * Now it uses JAudioTagger Library
	 * 
	 * @param path Path of the mp3 file to edit Tag
	 */
	private boolean editSongTag(Path path) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
			MP3File musicFile = null;
			try {
				musicFile = (MP3File) AudioFileIO.read(path.toFile());
			} catch (CannotReadException | IOException | TagException
					| ReadOnlyFileException | InvalidAudioFrameException e5) {
				throw e5;
			}
			System.out.println("Mp3: "+path+"\n"
					+"Has ID3v2Tag? "+musicFile.hasID3v2Tag()+"\n"
					+"Has ID3v1Tag? "+musicFile.hasID3v1Tag());
			String songArtist 		= null;
			String songTitle 		= null;
			String songAlbum 		= null;
			String songGenre3v24 	= null;
			//Get useful Tag information from either ID3v2 or ID3v1 if v2 does not exist
			if(musicFile.hasID3v2Tag()) {
				ID3v24Tag id3v24 = (ID3v24Tag) musicFile.getID3v2TagAsv24();
				songArtist = id3v24.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
				songTitle =  id3v24.getFirst(ID3v24Frames.FRAME_ID_TITLE);
				songAlbum = id3v24.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
				songGenre3v24 = findGenreString(id3v24.getFirst(ID3v24Frames.FRAME_ID_GENRE));
			}
			else if(musicFile.hasID3v1Tag()) {
				Tag tag = musicFile.getTag();
				songArtist = tag.getFirst(FieldKey.ARTIST);
				songTitle =  tag.getFirst(FieldKey.TITLE);
				songAlbum =  tag.getFirst(FieldKey.ALBUM);
				songGenre3v24 = findGenreString(tag.getFirst(FieldKey.GENRE));
			}
			else {
				throw new TagException();
			
			}
			//Create new ID3v24 Tag
			ID3v24Tag newID3v24Tag = new ID3v24Tag();
			try {
				newID3v24Tag.setField(FieldKey.ARTIST, songArtist);
				newID3v24Tag.setField(FieldKey.TITLE, songTitle);
				newID3v24Tag.setField(FieldKey.GENRE, songGenre3v24);
				newID3v24Tag.setField(FieldKey.ALBUM, songAlbum);
			} catch (KeyNotFoundException | FieldDataInvalidException e) {
				// TODO Auto-generated catch block
				System.out.println("ERROR: new ID3v24 tag could not be created "+newID3v24Tag.toString());
				throw e;
			}
			//Delete ID3v1 Tag
			ID3v1Tag v1Tag = musicFile.getID3v1Tag();
			try {
				if(musicFile.hasID3v1Tag())
					musicFile.delete(v1Tag);
			} catch (IOException e) {
				System.out.println("ERROR: Could not delete ID3v1 tag from mp3 file"+path);
				throw e;
			}
			//Set ID3v24 Tag to newly created ID3v2 tag
			musicFile.setID3v2Tag(newID3v24Tag);
			return true;
	}
	
	/**
	 * Description: This is made because JAudioTagger is VERY buggy in finding Genre
	 * JAudioTagger will either return a String Genre, a String Genre Number, or a String Genre Number in paranthesis...
	 * This function will convert
	 * 
	 * @return the Genre String that is not the number ID
	 */
	private String findGenreString(String songGenre) {
		try {
			if(songGenre.charAt(0) == '(') {
				songGenre = songGenre.substring(1, songGenre.length() - 1);
			}
			String songGenreString = GenreTypes.getInstanceOf().getValueForId(Integer.valueOf(songGenre));
			return songGenreString;
		} catch(NumberFormatException e) {
			return songGenre;
		}
	}
		
	/**
	 * Description: This method will organize the mp3 file. Uses Mp3File class from mp3agic library in order to analyze TAGS of song.
	 * The new path is created from these TAGS. The new file is then created using Files.move
	 * 
	 * @param path Original path of mp3 file to be organized
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws ReadOnlyFileException
	 * @throws CannotReadException
	 * @throws TagException
	 * @throws InvalidAudioFrameException
	 */
	private String organizeSong(Path path) throws Exception, IOException, ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException {
		MP3File mp3File = null;
		try {
			mp3File = (MP3File) AudioFileIO.read(path.toFile());
		} catch (CannotReadException | IOException | TagException
				| ReadOnlyFileException | InvalidAudioFrameException e1) {
			// TODO Auto-generated catch block
			throw e1;
		}

		//Creates the new Path using MP3File Tags
		String newFolder = createNewFolderPath(path, mp3File);
		String newFileName = createNewFileName(mp3File);
		String newPath = newFolder.concat(newFileName);
		
		//If new path is not the same as the old path
		if(!path.normalize().toString().equalsIgnoreCase(newPath)) {
			Path newMusicPath = Paths.get(newPath);
			Path newFolderPath = Paths.get(newFolder);
			//If the new Folder does
			if(Files.notExists(newFolderPath, LinkOption.NOFOLLOW_LINKS))  {
				try {
					Files.createDirectories(newFolderPath);
				} catch (IOException e) {
					throw e;
				}
			}
			String pathNoExt = newMusicPath.normalize().toString().substring(0, newMusicPath.normalize().toString().length()-4);
			int i = 0;
			while(true) {
				try {
					Files.move(path, newMusicPath);
					mp3File = null;
					return newMusicPath.toString();
				} catch (IOException e) {
					i++;
					if(i > 6) {
						throw e;
					}
					newMusicPath = Paths.get(pathNoExt+"("+i+")"+".mp3");
				}
			}
		}
		else {
			mp3File = null;
			return "File already organized correctly";
		}
	}
	
	/**
	 * Description: Creates the new file name from the mp3 tags
	 * 
	 * @param mp3File The MP3File used to get the tags
	 * @return The String of the new file name based on tag information
	 */
	private String createNewFileName(MP3File mp3File) {
		String artist = null;
		String title = null;
		if(mp3File.hasID3v2Tag()){
			artist = mp3File.getID3v2TagAsv24().getFirst(FieldKey.ARTIST);
			title = mp3File.getID3v2TagAsv24().getFirst(FieldKey.TITLE);
			System.out.print("CHECK Tag ID3v2...Artist "+artist+"... Title "+title);
		}
		else if(mp3File.hasID3v1Tag()) {
			artist = mp3File.getID3v1Tag().getFirst(FieldKey.ARTIST);
			title = mp3File.getID3v1Tag().getFirst(FieldKey.TITLE);
			System.out.print("CHECK Tag ID3v1...Artist "+artist+"... Title "+title);
		}
		else {
			System.out.println("ERROR: Could not find ID3v2 or ID3v1 Tags for mp3 "+mp3File.toString());
			System.exit(0);
		}
		return ("\\"+artist + " - " + title + ".mp3");
	}
	
	/**
	 * Description: This method creates a new folder path from the original mp3 file path. To do this it will analyze the mp3 files TAGS
	 * 	and determines how to create the path according the the user specifications. Used in organizeFile(Path path) method.
	 * 
	 * 
	 * @param path		The original path of the mp3 file
	 * @param mp3File	the Mp3File file that is created from the path
	 * @return			the String of the newFolder path (not including the file name)
	 */
	public String createNewFolderPath(Path path, MP3File mp3File) {
		
		String newPath = rootMusicFolderPath.normalize().toString();
		
		ID3v24Tag tag = (ID3v24Tag) mp3File.getID3v2TagAsv24();
		
		
		if(MusicOrganizerMain.option1.compareTo("Genre") == 0) {
			newPath = newPath.concat("\\" + findGenreString(tag.getFirst(ID3v24Frames.FRAME_ID_GENRE)));
		}
		else if(MusicOrganizerMain.option1.compareTo("Artist") == 0) {
			newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST));
		}	
		else if(MusicOrganizerMain.option1.compareTo("Album") == 0) {
			if(tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM) == null) {
				newPath = newPath.concat("\\Unknown Album");
			}
			else
				newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM));
		}
		else if(MusicOrganizerMain.option1.compareTo("Null") == 0) {
		}
		if(MusicOrganizerMain.option2.compareTo("Genre") == 0) {
			newPath = newPath.concat("\\" + findGenreString(tag.getFirst(ID3v24Frames.FRAME_ID_GENRE)));
		}
		else if(MusicOrganizerMain.option2.compareTo("Artist") == 0) {
			newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST));
		}	
		else if(MusicOrganizerMain.option2.compareTo("Album") == 0) {
			if(tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM) == null) {
				newPath = newPath.concat("\\Unknown Album");
			}
			else
				newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM));
		}
		else if(MusicOrganizerMain.option2.compareTo("Null") == 0) {
		}
		if(MusicOrganizerMain.option3.compareTo("Genre") == 0) {
			newPath = newPath.concat("\\" + findGenreString(tag.getFirst(ID3v24Frames.FRAME_ID_GENRE)));
		}
		else if(MusicOrganizerMain.option3.compareTo("Artist") == 0) {
			newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST));
		}	
		else if(MusicOrganizerMain.option3.compareTo("Album") == 0) {
			if(tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM) == null) {
				newPath = newPath.concat("\\Unknown Album");
			}
			else
				newPath = newPath.concat("\\"+tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM));
		}
		else if(MusicOrganizerMain.option3.compareTo("Null") == 0) {
		}
		return newPath;
	}
	
	/**
	 * Description: Will directory a music directory, the directory could be the root library directory or any subfolder from it.
	 * It will organize any mp3 file it finds to its appropriate folder.
	 * It will execute a new working Runnable task to the MusicOrganizerMain.threadPool for each new directory found within this current directory.
	 * Directories that have already been organized will never be re-organized by this method. Will delete an iterate directory if it is empty and exists 
	 * and will also delete the original music directory (not necessarily the root, but a subdirectory of the root) if it is empty at the end.
	 */
	public void OrganizeMusicDirectory() {
		for(Path filePath : pathsStream) {
			BasicFileAttributes fileAttr = null;
			try {
				fileAttr = Files.getFileAttributeView(filePath, BasicFileAttributeView.class).readAttributes();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error on getting attributeView for "+filePath.normalize().toString());
				System.exit(0);
			}
			//Is File && file has not been organized recently
			if(Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) && (!MusicOrganizerMain.organizeRecent || (3600000 > (MusicOrganizerMain.currentDate.getTime() - fileAttr.creationTime().toMillis())))) {
				if(filePath.normalize().toString().endsWith(".mp3")) {
					//Mp3 File has not been organized recently
					if(!MusicOrganizerMain.organizeRecent || (3600000 > (MusicOrganizerMain.currentDate.getTime() - fileAttr.creationTime().toMillis()))) {
						//OPTIONAL: Makes all songs have uniform tag versions (ID3v2.4)
						if(MusicOrganizerMain.editSongTags == true) {
							//Tries to Edit mp3 Tags to make them all uniform
								try {
									editSongTag(filePath);
									try {
										MusicOrganizerMain.semTextAppending.acquire();
									} catch (InterruptedException e) {
									}
									MusicOrganizerMain.activitiesLog.append("Edited Mp3 Tags for.."+filePath.normalize().toString()+"\n");
									MusicOrganizerMain.semTextAppending.release();
								} catch (CannotReadException | IOException
										| TagException | ReadOnlyFileException
										| InvalidAudioFrameException e) {
									e.printStackTrace();
									System.out.println("ERROR: Could not find Id3v1 or Id3v2 tags in file "+filePath+"\n");
									try {
										MusicOrganizerMain.semTextAppending.acquire();
									} catch (InterruptedException e1) {
									}
									MusicOrganizerMain.activitiesLog.append("ERROR: Could not find Id3v1 or Id3v2 tags in file"+filePath.normalize().toString()+"\n");
									MusicOrganizerMain.semTextAppending.release();
								}
						}
						//Tries to organizes all songs into correct directories
						try {
							String newPath = organizeSong(filePath);
							MusicOrganizerMain.semTextAppending.acquire();
							MusicOrganizerMain.activitiesLog.append("File Organized Correctly from.. "+filePath.normalize().toString()
									+"                  to... " + newPath);
							MusicOrganizerMain.semTextAppending.release();
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("ERROR: organizeSong(Path) failed for... "+filePath.normalize().toString()+"\n"
									+"        Reasons: File cannot be accessed OR greater than 6 duplicates\n");
							try {
								MusicOrganizerMain.semTextAppending.acquire();
								MusicOrganizerMain.activitiesLog.append("ERROR: organizeSong(Path) failed for... "+filePath.normalize().toString()+"\n"
										+"        Reasons: File cannot be accessed OR greater than 6 duplicates\n");
								MusicOrganizerMain.semTextAppending.release();
							}catch(InterruptedException e4) {
								
							}
						}
					}
				}
			}
			else if(Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS)) {
				try {
					if(!Files.newDirectoryStream(filePath).iterator().hasNext()) {
						Files.deleteIfExists(filePath);
					}
					else {
						Runnable worker = new MusicOrganizer(filePath);
						MusicOrganizerMain.sem.acquire();
						MusicOrganizerMain.numTasks++;
						MusicOrganizerMain.threadPool.execute(worker);
						MusicOrganizerMain.sem.release();
						System.out.println("Added task to pool");
						//MusicOrganizerMain.semTextAppending.acquire();
						//MusicOrganizerMain.activitiesLog.append("Added task to pool"+"\n");
						//MusicOrganizerMain.semTextAppending.release();
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					System.out.println("Error on creating directory stream for "+filePath.normalize().toString());
					System.exit(0);
				}
			}
		}
		try {
			if(!Files.newDirectoryStream(musicDir).iterator().hasNext()) {
				Files.deleteIfExists(musicDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error on creating directory stream for "+musicDir.normalize().toString());
			System.exit(0);
		}
	}
	
	/**
	 * Description: Removes all empty Folders in a given directory, a directory stream is used to iterate through each directory. 
	 * Uses recursion to perform folder removal, because it needs to start from the furthest directory node.
	 * 
	 * @param directory The Directory that is to be 
	 */
	public void removeEmptyFolders(Path directory) {
		DirectoryStream<Path> pathsStream = null;
		try {
			pathsStream = Files.newDirectoryStream(musicDir);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR: on initiating pathsStream to newDirectoryStreame"+directory);
			System.exit(0);
		}
		Path filePath;
		while(pathsStream.iterator().hasNext()) {
			filePath = pathsStream.iterator().next();
			if(Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS)) {
				removeEmptyFolders(filePath);
				try {
					Files.deleteIfExists(filePath);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("ERROR: on deleteing empty directory either because not empty, IO error, or security error");
					try {
						pathsStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						System.out.println("Could not close pathsStream in folderRemover");
						System.exit(0);
					}
					System.exit(0);
				}
				
			}
			try {
				pathsStream.close();
			} catch (IOException e) {				
				e.printStackTrace();
				System.out.println("ERROR: couldn't close pathStream");
				System.exit(0);
			}	
		}
	}
}

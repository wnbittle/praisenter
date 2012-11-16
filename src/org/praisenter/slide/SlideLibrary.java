package org.praisenter.slide;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.praisenter.Constants;
import org.praisenter.xml.FileProperties;
import org.praisenter.xml.Thumbnail;
import org.praisenter.xml.XmlIO;

/**
 * Static interface for loading and saving slides and templates.
 * @author USWIBIT
 *
 */
public class SlideLibrary {
	/** The class level logger */
	private static final Logger LOGGER = Logger.getLogger(SlideLibrary.class);
	
	// storage
	
	/** The saved slides */
	private static final Map<String, Slide> SLIDES = new HashMap<String, Slide>();
	
	/** The saved templates */
	private static final Map<String, SlideTemplate> TEMPLATES = new HashMap<String, SlideTemplate>();
	
	/** The saved bible templates */
	private static final Map<String, BibleSlideTemplate> BIBLE_TEMPLATES = new HashMap<String, BibleSlideTemplate>();
	
	/** The saved song templates */
	private static final Map<String, SongSlideTemplate> SONG_TEMPLATES = new HashMap<String, SongSlideTemplate>();

	/** The saved notification templates */
	private static final Map<String, NotificationSlideTemplate> NOTIFICATION_TEMPLATES = new HashMap<String, NotificationSlideTemplate>();
	
	// thumbnails
	
	/** The thumbnail size */
	private static final Dimension THUMBNAIL_SIZE = new Dimension(64, 48);
	
	/** The thumbnail file name */
	private static final String THUMBS_FILE = Constants.SEPARATOR + "_thumbs.xml";
	
	/** The list of all thumbnails */
	private static final Map<String, Thumbnail> THUMBNAILS = new HashMap<String, Thumbnail>();

	// state
	
	/** True if the slide library has been loaded */
	private static boolean loaded = false;
	
	// FIXME translation on exceptions
	static {
		// FIXME move this
		try {
			loadSlideLibrary();
		} catch (SlideLibraryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Hidden constructor */
	private SlideLibrary() {}

	/**
	 * Loads the slide and template library.
	 * @throws SlideLibraryException thrown if an exception occurs during loading
	 */
	public static final synchronized void loadSlideLibrary() throws SlideLibraryException {
		if (!loaded) {
			// preload the thumbnails, slides, and templates for all slides and templates
			loadSlideLibrary(Constants.SLIDE_PATH, Slide.class, SLIDES);
			loadSlideLibrary(Constants.TEMPLATE_PATH, SlideTemplate.class, TEMPLATES);
			loadSlideLibrary(Constants.BIBLE_TEMPLATE_PATH, BibleSlideTemplate.class, BIBLE_TEMPLATES);
			loadSlideLibrary(Constants.NOTIFICATIONS_TEMPLATE_PATH, NotificationSlideTemplate.class, NOTIFICATION_TEMPLATES);
			loadSlideLibrary(Constants.SONGS_TEMPLATE_PATH, SongSlideTemplate.class, SONG_TEMPLATES);
			loaded = true;
		}
	}
	
	/**
	 * Loads the thumbnails file from the given path.
	 * <p>
	 * If the file does not exist or is out of sync, it is generated and saved.
	 * @param path the path for the thumbnail file
	 * @param clazz the type to load
	 * @param map the map to add the loaded slide/template
	 * @throws SlideLibraryException thrown if the slide failed to be loaded
	 */
	private static final synchronized <E extends Slide> void loadSlideLibrary(String path, Class<E> clazz, Map<String, E> map) throws SlideLibraryException {
		// attempt to read the thumbs file in the respective folder
		List<Thumbnail> thumbnailsFromFile = null;
		try {
			SlideThumbnails sts = XmlIO.read(path + THUMBS_FILE, SlideThumbnails.class);
			if (sts != null) {
				thumbnailsFromFile = sts.getThumbnails();
			}
		} catch (FileNotFoundException e) {
			// just eat this one
		} catch (Exception e) {
			// silently ignore this error
			LOGGER.error("Could not read [" + path + THUMBS_FILE + "]: ", e);
		}
		if (thumbnailsFromFile == null) {
			thumbnailsFromFile = new ArrayList<Thumbnail>();
		}
		
		// create a new list to store the thumbnails
		List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
		// track whether we need to resave the thumbnail XML
		boolean save = false;
		
		// read the media library file names
		File[] files = new File(path).listFiles();
		if (files != null) {
			for (File file : files) {
				// skip directories
				if (file.isDirectory()) continue;
				// get the file path
				String filePath = file.getPath();
				// skip the thumbnail file
				if (filePath.contains(THUMBS_FILE)) continue;
				// make sure there exists a thumnail for the file
				boolean exists = false;
				for (Thumbnail thumb : thumbnailsFromFile) {
					if (thumb.getFileProperties().getFileName().equals(file.getName())) {
						// flag that the thumbnail exists
						exists = true;
						// add it to the thumbnails array
						thumbnails.add(thumb);
						// we can break from the loop
						break;
					}
				}
				// check if we need to generate a thumbnail for the file
				if (!exists) {
					// generate a thumbnail for the image using the media loader
					// load the media
					E slide = loadFromSlideLibrary(filePath, clazz);
					// add the media to the media library (might as well since we loaded it)
					map.put(filePath, slide);
					// create the thumbnail
					BufferedImage image = slide.getThumbnail(THUMBNAIL_SIZE);
					// add the thumbnail to the list
					thumbnails.add(new Thumbnail(FileProperties.getFileProperties(filePath), image));
					// flag that we need to save it
					save = true;
				} else {
					// we need to add a media reference anyway
					map.put(filePath, null);
				}
			}
			// add all the thumbnails
			for (Thumbnail thumbnail : thumbnails) {
				THUMBNAILS.put(thumbnail.getFileProperties().getFilePath(), thumbnail);
			}
			// after we have read all the files we need to save the new thumbs xml
			if (save || thumbnailsFromFile.size() != thumbnails.size()) {
				saveThumbnailsFile(clazz);
			}
		}
	}
	
	/**
	 * Returns the file system path for the given class.
	 * @param clazz the class
	 * @return String
	 */
	private static final String getPath(Class<?> clazz) {
		String path = Constants.SLIDE_PATH;
		
		// see if the type is a template
		if (Template.class.isAssignableFrom(clazz)) {
			// it could be a bible, song, or notification template
			if (BibleSlide.class.isAssignableFrom(clazz)) {
				path = Constants.BIBLE_TEMPLATE_PATH;
			} else if (NotificationSlide.class.isAssignableFrom(clazz)) {
				path = Constants.NOTIFICATIONS_TEMPLATE_PATH;
			} else if (SongSlide.class.isAssignableFrom(clazz)) {
				path = Constants.SONGS_TEMPLATE_PATH;
			} else {
				// generic template
				path = Constants.TEMPLATE_PATH;
			}
		}
		
		return path;
	}
	
	/**
	 * Writes the thumbnails file for the given class.
	 * @param clazz the class type
	 */
	private static synchronized final void saveThumbnailsFile(Class<?> clazz) {
		// get the path and thumbnails for the given class type
		String path = getPath(clazz);
		List<Thumbnail> thumbnails = getThumbnails(clazz);
		
		try {
			XmlIO.save(path + THUMBS_FILE, new SlideThumbnails(thumbnails));
			LOGGER.info("File [" + path + THUMBS_FILE + "] updated.");
		} catch (JAXBException | IOException e) {
			// silently log this error
			LOGGER.error("Failed to re-save [" + path + THUMBS_FILE + "]: ", e);
		}
	}

	/**
	 * Loads the given slide from the slide library.
	 * @param filePath the file name and path
	 * @param clazz the type to load
	 * @return {@link Slide}
	 * @throws SlideLibraryException thrown if the slide failed to be loaded
	 */
	private static synchronized final <E extends Slide> E loadFromSlideLibrary(String filePath, Class<E> clazz) throws SlideLibraryException {
		try {
			return XmlIO.read(filePath, clazz);
		} catch (JAXBException | IOException e) {
			throw new SlideLibraryException(MessageFormat.format("Could not load slide [{0}]", filePath), e);
		}
	}
	
	// public interface
	
	// thumbnails
	
	/**
	 * Returns the thumbnail for the given file path.
	 * <p>
	 * Returns null if no thumbnail exists.
	 * @param filePath the file path
	 * @return {@link Thumbnail}
	 */
	public static final synchronized Thumbnail getThumbnail(String filePath) {
		return THUMBNAILS.get(filePath);
	}

	/**
	 * Returns the thumbnails for the given class.
	 * @param clazz the class
	 * @return List&lt;{@link Thumbnail}&gt;
	 */
	public static final synchronized List<Thumbnail> getThumbnails(Class<?> clazz) {
		// we can use the slides map to get all the file path/names and use
		// those to look up all the thumbnails in that directory
		Set<String> paths = SLIDES.keySet();
		// see if the type is a template
		if (Template.class.isAssignableFrom(clazz)) {
			// it could be a bible, song, or notification template
			if (BibleSlide.class.isAssignableFrom(clazz)) {
				paths = BIBLE_TEMPLATES.keySet();
			} else if (NotificationSlide.class.isAssignableFrom(clazz)) {
				paths = NOTIFICATION_TEMPLATES.keySet();
			} else if (SongSlide.class.isAssignableFrom(clazz)) {
				paths = SONG_TEMPLATES.keySet();
			} else {
				// generic template
				paths = TEMPLATES.keySet();
			}
		}
		// get the thumbnails
		List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
		for (String filePath : paths) {
			thumbnails.add(THUMBNAILS.get(filePath));
		}
		return thumbnails;
	}
	
	// slide library
	
	/**
	 * Returns the saved slide.
	 * @param filePath the file name and path of the saved slide
	 * @return {@link Slide}
	 * @throws SlideLibraryException thrown if an exception occurs while loading the slide
	 */
	public static final synchronized Slide getSlide(String filePath) throws SlideLibraryException {
		try {
			return XmlIO.read(filePath, Slide.class);
		} catch (JAXBException | IOException e) {
			throw new SlideLibraryException(e);
		}
	}
	
	/**
	 * Adds the given slide to the slide library.
	 * @param fileName the desired file name
	 * @param slide the slide
	 * @throws SlideLibraryException thrown if an error occurs saving the slide
	 */
	public static final synchronized void saveSlide(String fileName, Slide slide) throws SlideLibraryException {
		String filePath = Constants.SLIDE_PATH + Constants.SEPARATOR + fileName + ".xml";
		try {
			// save the slide to disc
			XmlIO.save(filePath, slide);
			// add the slide to the slide library
			SLIDES.put(filePath, slide);
			// create a thumbnail
			Thumbnail thumbnail = new Thumbnail(
					FileProperties.getFileProperties(filePath), 
					slide.getThumbnail(THUMBNAIL_SIZE));
			// add the thumbnail to the list of thumbnails
			THUMBNAILS.put(filePath, thumbnail);
			// update the thumbnail file for this directory
			saveThumbnailsFile(Slide.class);
		} catch (JAXBException e) {
			throw new SlideLibraryException(e);
		} catch (IOException e) {
			throw new SlideLibraryException(e);
		}
	}
	
	/**
	 * Removes the given slide.
	 * @param filePath the file name and path
	 * @return boolean
	 */
	public static final synchronized boolean deleteSlide(String filePath) {
		File file = new File(filePath);
		// delete the file
		if (file.delete()) {
			// remove the weak reference
			SLIDES.remove(filePath);
			// remove the thumbnail
			THUMBNAILS.remove(filePath);
			return true;
		}
		return false;
	}
	
	// template library
	
	/**
	 * Returns the saved template.
	 * @param filePath the file name and path of the saved template
	 * @param clazz the type of the template
	 * @return {@link Slide}
	 * @throws SlideLibraryException thrown if an exception occurs while loading the template
	 */
	public static final synchronized <E extends Slide & Template> E getTemplate(String filePath, Class<E> clazz) throws SlideLibraryException {
		try {
			return XmlIO.read(filePath, clazz);
		} catch (JAXBException | IOException e) {
			throw new SlideLibraryException(e);
		}
	}
	
	/**
	 * Saves the given template to the slide library.
	 * @param fileName the file name
	 * @param template the template
	 * @throws SlideLibraryException thrown if an exception occurs while saving the template
	 */
	public static final synchronized <E extends Slide & Template> void saveTemplate(String fileName, E template) throws SlideLibraryException {
		String path = getPath(template.getClass());
		String filePath = path + Constants.SEPARATOR + fileName + ".xml";
		try {
			// save the template to disc
			XmlIO.save(filePath, template);
			// add the template to the slide library
			if (template instanceof BibleSlideTemplate) {
				BIBLE_TEMPLATES.put(filePath, (BibleSlideTemplate)template);
			} else if (template instanceof NotificationSlideTemplate) {
				NOTIFICATION_TEMPLATES.put(filePath, (NotificationSlideTemplate)template);
			} else if (template instanceof SongSlideTemplate) {
				SONG_TEMPLATES.put(filePath, (SongSlideTemplate)template);
			} else {
				TEMPLATES.put(filePath, (SlideTemplate)template);
			}
			// create a thumbnail
			Thumbnail thumbnail = new Thumbnail(
					FileProperties.getFileProperties(filePath), 
					template.getThumbnail(THUMBNAIL_SIZE));
			// add the thumbnail to the list of thumbnails
			THUMBNAILS.put(filePath, thumbnail);
			// update the thumbnail file for this directory
			saveThumbnailsFile(template.getClass());
		} catch (JAXBException e) {
			throw new SlideLibraryException(e);
		} catch (IOException e) {
			throw new SlideLibraryException(e);
		}
	}

	/**
	 * Removes the given template.
	 * @param filePath the file name and path
	 * @return boolean
	 */
	public static final synchronized boolean deleteTemplate(String filePath) {
		File file = new File(filePath);
		// delete the file
		if (file.delete()) {
			// remove the weak reference
			TEMPLATES.remove(filePath);
			// remove the thumbnail
			THUMBNAILS.remove(filePath);
			return true;
		}
		return false;
	}
}

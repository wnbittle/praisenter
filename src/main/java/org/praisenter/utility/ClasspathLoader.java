package org.praisenter.utility;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ClasspathLoader {
	private ClasspathLoader() {}
	
	public static final BufferedImage getBufferedImage(String path) {
		try {
			URL url = ClasspathLoader.class.getResource(path);
			return ImageIO.read(url);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static final Image getImage(String path) {
		return new Image(path, true);
	}
	
	public static final ImageIcon getIcon(String path) {
		try {
			URL url = ClasspathLoader.class.getResource(path);
			BufferedImage image = ImageIO.read(url);
			return new ImageIcon(image);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static final void copy(String from, Path to) throws FileNotFoundException, IOException {
		// get the classpath resource
		try (InputStream is = ClasspathLoader.class.getResourceAsStream(from)) {
			// see if we found the classpath resource
			if (is == null) {
				throw new FileNotFoundException();
			}
			
			Files.copy(is, to);
		}
	}
}
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.praisenter.data.song.Author;
import org.praisenter.data.song.ChurchViewSongReader;
import org.praisenter.data.song.Song;
import org.praisenter.data.song.SongLibrary;
import org.praisenter.data.song.Title;
import org.praisenter.xml.XmlIO;

/**
 * Workflow:
 * Import -> XML File -> Jaxb Unmarshal -> Add data to lucene index -> Marshal to song dir
 * Add New -> User Input -> Add data to lucene index -> Marshal to song dir
 * 
 * @author William
 *
 */
public class TestSongLibrary {
	public static void main(String[] args) {
		try {
			Path path = Paths.get("C:\\Users\\William\\Desktop\\test\\songs");
			
			Map<String, Integer> nameMap = new HashMap<String, Integer>();
			ChurchViewSongReader reader = new ChurchViewSongReader();
			List<Song> songs = reader.read(Paths.get("C:\\Users\\William\\Desktop\\test\\ChurchViewSongs.cvDat"));
			
			for (Song song : songs) {
				Title title = song.getDefaultTitle();
				String variant = song.getProperties().getVariant();
				Author author = song.getDefaultAuthor();
				
				String name = (title != null ? title.getText().replaceAll("\\W+", "_") : "") +
							  (variant != null && variant.length() > 0 ? "_" + variant.replaceAll("\\W+", "_") : "") +
							  (author != null ? "_" + author.getName().replaceAll("\\W+", "_") : "");
				
				Path dest = path.resolve(name + ".xml");
				if (nameMap.containsKey(name + ".xml")) {
					Integer n = nameMap.get(name + ".xml");
					n++;
					dest = path.resolve(name + String.valueOf(n) + ".xml");
					nameMap.put(name + ".xml", n);
				} else {
					nameMap.put(name + ".xml", 1);
				}
				
				XmlIO.save(dest, song);
			}
			
			SongLibrary sl = SongLibrary.open(path);
			
			System.out.println("-------------------------------------------------------------");
			
			sl.search("morning");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
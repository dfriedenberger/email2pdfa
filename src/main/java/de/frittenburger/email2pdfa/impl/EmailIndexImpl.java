package de.frittenburger.email2pdfa.impl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;

import de.frittenburger.email2pdfa.interfaces.EmailIndex;
import de.frittenburger.email2pdfa.interfaces.Index;
import de.frittenburger.email2pdfa.interfaces.NameService;

public class EmailIndexImpl implements EmailIndex {

	private String emailIndexFilePath = null;
	private final NameService nameService = new NameServiceImpl();

	private Map<String,Map<String,Index>> map = new HashMap<String,Map<String,Index>>();
	private Set<String> messageKeyCache = new HashSet<String>();
	private Index get(String store, String folder) {
		
		if(!map.containsKey(store))
			map.put(store, new HashMap<String,Index>());
		
		if(!map.get(store).containsKey(folder))
			map.get(store).put(folder, new IndexImpl());
		
		return map.get(store).get(folder);
		
	}

	@Override
	public void register(String store, String folder, String messageKey, int index) {
		messageKeyCache.add(messageKey);
		get(store,folder).register(index,messageKey);
	}
	
	
	@Override
	public void registerError(String store, String folder, int index) {
		get(store,folder).register(index,null);
		
	}

	@Override
	public int getUpperMost(String store, String folder) {
		return get(store,folder).getUpperMost();
	}

	@Override
	public int getIndex(String store, String folder, String messageKey) {
		return get(store,folder).getIndex(messageKey);
	}

	@Override
	public void clear(String store, String folder,int s,int e) {
		get(store,folder).remove(s,e);
	}

	@Override
	public void init(String emailIndexFilePath) throws IOException {
		this.emailIndexFilePath = emailIndexFilePath;	
		
	
		String[] names = new File(emailIndexFilePath).list(new WildcardFilter("index_*.txt"));
		
		
		for(String n : names)
		{
			List<String> lines = Files.readAllLines(Paths.get(emailIndexFilePath + "/" + n), StandardCharsets.UTF_8);
			for(String line:lines){
				
				List<String> list = new ArrayList<String>();
				Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
				while (m.find())
				    list.add(m.group(1).replaceAll("^\"|\"$", ""));
				if(list.size() != 4)
					throw new IOException("invalid index file "+line);
				
				String store = list.get(0);
				String folder = list.get(1);
				int index = Integer.parseInt(list.get(2));
				String messageKey = list.get(3).equals("null") ? null : list.get(3);
				get(store,folder).register(index,messageKey);
			}
		}
		
	}
	
	
	
	
	@Override
	public void commit() {
	
			try {
				
				for(String store : map.keySet())
				{
				
					String filename = emailIndexFilePath + "/"
							+ nameService.parseValidFilename("index_"+store, new ContentType("text","plain",null));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(
						    new FileOutputStream(filename), StandardCharsets.UTF_8), true);
				
					for(String folder : map.get(store).keySet())
					{
						Index ix = map.get(store).get(folder);
						if(ix instanceof IndexImpl)
						{
							IndexImpl index = (IndexImpl)ix;
							int max = index.recalculateMax();
							Map<Integer, String> m = index.getIndexes();
							for(int i = 1;i <= max;i++)
							{
								String line = String.format("\"%s\" \"%s\" %d %s", store,folder,i,m.containsKey(i) ? m.get(i): "null");
								out.println(line);
							}
							
						}
					}
					out.close();
				}
			} catch (IOException e) {
			   e.printStackTrace();
			}
		}

	

}

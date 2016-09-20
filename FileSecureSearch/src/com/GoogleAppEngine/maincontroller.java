package com.GoogleAppEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.GoogleAppEngine.jdo.FileJdo;
import com.GoogleAppEngine.jdo.PMF;
import com.GoogleAppEngine.jdo.UserDetailsJdo;
import com.GoogleAppEngine.jdo.UserTrackJdo;
import com.GoogleAppEngine.utilities.SearchUtil;
import com.GoogleAppEngine.utilities.SendEmail;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.labs.repackaged.org.json.Cookie;
import com.google.common.base.Strings;





@Controller
@SuppressWarnings("unchecked")
public class maincontroller 
{
	Logger logger=Logger.getLogger("filesecureController");
	JSONParser jsonParser=new JSONParser();
	JSONObject responseJson=null;
	
	
	
	/**
	 * 
	 * @return filesInfo page
	 * @throws IOException 
	 */
	@RequestMapping(value="/filesInfo", method=RequestMethod.GET)
	public ModelAndView timezoneURL(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		HttpSession session= req.getSession(true);
		if(session.getAttribute("admin") == null)
			res.sendRedirect("/login");
		System.out.println("/filesinfo");
		ModelAndView m=new ModelAndView("fileinfo");
		List<FileJdo> retVal= new ArrayList<FileJdo>();
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		HashMap<Integer,List<String>> hs=new HashMap<Integer,List<String>>();
		int i=0;
		Query q=pm.newQuery(FileJdo.class);
		@SuppressWarnings("unchecked")
		List<FileJdo> li=(List<FileJdo>) q.execute();
		for(FileJdo cd:li)
		{			
			System.out.println("filejdo "+ cd.getFileName());
			retVal.add(cd);
			System.out.println("retVal "+ retVal);
			
			ArrayList<String> each=new ArrayList<String>();
			
			each.add(cd.getUserName());
			each.add(cd.getUserEmail());
			each.add(cd.getFileName());
			each.add(cd.getDateAdded().toString());
			
			hs.put(i, each);
			
			i++;
		}
		
		
		m.addObject("files",hs);
		return m;
	}
	
	
	
	
	/**
	 * 
	 * @return changePasswordRequest page
	 */
	@RequestMapping(value="/changePasswordRequest", method=RequestMethod.GET)
	public String changePasswordRequest()
	{
		System.out.println("/changePasswordRequest");
		return "changePasswordRequest";
	}

	

	
	/**
	 * 
	 * @return upload page
	 * @throws IOException 
	 */
	@RequestMapping(value="/upload", method=RequestMethod.GET)
	public String uploadRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		HttpSession session= req.getSession(true);
		if(session.getAttribute("admin") == null)
			res.sendRedirect("/login");
		
		System.out.println("/upload");
		return "upload";
	}
	
	
	/**
	 * 
	 * @return show user Downloaded File page
	 */
	@RequestMapping(value="/downloades", method=RequestMethod.GET)
	public ModelAndView fileDownloads(HttpServletRequest req, HttpServletResponse res)
	{
		System.out.println("/userFileInfo");
		ModelAndView m=new ModelAndView("userFileInfo");
		List<UserTrackJdo> retVal= new ArrayList<UserTrackJdo>();
		
		HashMap<Integer,List<String>> hs=new HashMap<Integer,List<String>>();
		int i=0;
		
		

		HttpSession session= req.getSession(true);
		String UserName = session.getAttribute("userName").toString();
		String UserEmail = session.getAttribute("userid").toString();
		System.out.println(" user name " + UserName);
		System.out.println(" user email " + UserEmail);
		
		List<UserTrackJdo> li = getUserDownlodedFiles(UserEmail,UserName);
		System.out.println(" list size " + li.size());
		for(UserTrackJdo cd:li)
		{			
			System.out.println("filejdo "+ cd.getFileName());
			retVal.add(cd);
			System.out.println("retVal "+ retVal);
			
			ArrayList<String> each=new ArrayList<String>();
			
			each.add(cd.getUserName());
			each.add(cd.getEmail());
			each.add(cd.getFileName());
			each.add(cd.getDateDownloaded().toString());
			
			hs.put(i, each);
			
			i++;
		}
		
		
		m.addObject("files",hs);
		return m;
	}
	
	public static List <UserTrackJdo> getUserDownlodedFiles( String UserEmail , String UserName  )
	{
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		
		StringBuffer queryBuffer = new StringBuffer();
		if(UserEmail.equals("admin@mail.com")){
			queryBuffer.append( "SELECT FROM " + UserTrackJdo.class.getName() + " WHERE isDeleted == "+ false);
		}
		else
		{
			queryBuffer.append( "SELECT FROM " + UserTrackJdo.class.getName() + " WHERE userName == '"+ UserName +"' && isDeleted == "+ false);
		}
		
		//System.out.println(" queryBuffer " + queryBuffer);
		
		Query q = pm.newQuery( queryBuffer.toString() );
		
		//Query q=pm.newQuery(FileJdo.class);
		@SuppressWarnings("unchecked")
		List<UserTrackJdo> li=(List<UserTrackJdo>) q.execute();
		return li;
	}
	
	
	public static void updateVotes( String filekey)
	{
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		
		StringBuffer queryBuffer = new StringBuffer();
		
		queryBuffer.append( "SELECT FROM " + FileJdo.class.getName() + " WHERE ImageId == '"+ filekey +"' && isDeleted == "+ false);
		Query q = pm.newQuery( queryBuffer.toString() );
		List<FileJdo> li=(List<FileJdo>) q.execute();
		//System.out.println(" size "+ li.size());
		for(FileJdo fileObj:li){
			fileObj.setVotes(fileObj.getVotes()+1); // fileObj.getVotes();
			//System.out.println(" votes count "+ fileObj.getVotes());
		}
//		
//		String keyString=KeyFactory.createKeyString(FileJdo.class.getSimpleName(), fileName);
//		FileJdo fileObj= pm.getObjectById(FileJdo.class, keyString);
//		
		// fileObj.setVotes(fileObj.getVotes()+1); // fileObj.getVotes();
		
		//System.out.println(" votes count "+ fileObj.getVotes());
	}
	
	/**
	 * 
	 * @return show user Info page
	 * @throws IOException 
	 */
	@RequestMapping(value="/users", method=RequestMethod.GET)
	public ModelAndView userInfo(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		System.out.println("/userInfo");
		HttpSession session= req.getSession(true);
		if(session.getAttribute("admin") == null)
			res.sendRedirect("/login");
		ModelAndView m=new ModelAndView("userInfo");
		List<UserDetailsJdo> retVal= new ArrayList<UserDetailsJdo>();
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		HashMap<Integer,List<String>> hs=new HashMap<Integer,List<String>>();
		int i=0;
		
		StringBuffer queryBuffer = new StringBuffer();

		
		String UserName = session.getAttribute("userName").toString();
		String UserEmail = session.getAttribute("userid").toString();
		System.out.println(" user name " + UserName);
		System.out.println(" user email " + UserEmail);
		//queryBuffer.append( "SELECT FROM " + UserDetailsJdo.class.getName() + " WHERE isDeleted == "+ false);
		queryBuffer.append( "SELECT FROM " + UserDetailsJdo.class.getName());
		
		System.out.println(" queryBuffer " + queryBuffer);
		
		Query q = pm.newQuery( queryBuffer.toString() );
		
		//Query q=pm.newQuery(FileJdo.class);
		@SuppressWarnings("unchecked")
		List<UserDetailsJdo> li=(List<UserDetailsJdo>) q.execute();
		
		
		
		
		System.out.println(" list size " + li.size());
		for(UserDetailsJdo cd:li)
		{			
			List<UserTrackJdo> downloadList = getUserDownlodedFiles(cd.getEmail(),cd.getFirstname());
			
			String count = ""+downloadList.size();
			retVal.add(cd);
			System.out.println("retVal "+ retVal);
			
			ArrayList<String> each=new ArrayList<String>();
			
			
			each.add(cd.getFirstname());
			each.add(cd.getEmail());
			each.add(count);
			each.add(cd.getRole());
			if( UserEmail.equals(cd.getEmail()) || cd.isIsreqAdmin() == "false" || cd.getEmail().equals("admin@mail.com")){
				each.add("disabled");
				each.add("");
			}
			else if(cd.getRole().equals("Admin") ){
				each.add("Checked");
				each.add("");	
			}
			else if(cd.isIsreqAdmin() == "true" )
			{
				each.add("unChecked");
				each.add("Requested for Admin");
			}
			else{
				each.add("unChecked");
				each.add("");
			}
				
			hs.put(i, each);
			
			i++;
		}
		
		
		m.addObject("files",hs);
		return m;
	}
	
	/**
	 * 
	 * @return Search page
	 */
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public String search()
	{
		System.out.println("/search");
		return "search";
	}
	
	String uploadImageUrl					= "";
	String uploadImageblobKey						= "";
	int filecount=0;
	
	@RequestMapping(value="/blobcontroller",method=RequestMethod.POST)
	public void image(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{ 	
		filecount++;
		System.out.println("hi image");
		HttpSession session=req.getSession(true);	
		ImagesService imagesService 				= ImagesServiceFactory.getImagesService();
		BlobKey blobKey			= null;
		BlobKey blobKeyold			= null;
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		    	Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
		    	List<BlobKey> blobKeyList = blobs.get("myFile");
		    	if(filecount>1)
			    {
			    	for(BlobKey iteratingBlobKeyList : blobKeyList)
					{
						if(iteratingBlobKeyList != null)
						{
							blobKeyold					= iteratingBlobKeyList;
							System.out.println("blobKeyold------->"+blobKeyold);
						}
					}
			    }	    	
		    	blobKey						= blobKeyList.get(0);
		    	System.out.println("blob is-------->"+blobKey.getKeyString() );
		    	uploadImageUrl = blobKey.getKeyString() ;
		    	
		    	System.out.println("uploadImageUrl"+uploadImageUrl);
		        resp.getWriter().print(uploadImageUrl);
		    	blobKeyList.remove(blobKeyold);
	
	}
	
	
	@RequestMapping(value="/returnUrlsession",method=RequestMethod.POST)	
	public void returnUrl(@RequestParam(value="name", required=false)String fileName,HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		String ImageId= uploadImageUrl;
		Query pmquery	 = pm.newQuery(FileJdo.class);
		List<FileJdo> userBadgeLogInfo =  (List<FileJdo>)pmquery.execute();
		HttpSession session=req.getSession(true);	
		resp.setContentType("text/plain");
		System.out.println("userBadgeLogInfo------->"+session.getAttribute("userid").toString());
		resp.getWriter().print(uploadImageUrl);
		session.setAttribute("fileUrl",uploadImageUrl);
		//session.setAttribute("imageList",list);
		FileJdo obj=new FileJdo();
		obj.setUserEmail(session.getAttribute("userid").toString());
		obj.setUserName(session.getAttribute("userName").toString());
		obj.setDateAdded(new Date());
		obj.setIsDeleted(false);
		obj.setFileName(fileName);
		obj.setuploadFile(ImageId);
		List<String> tokens=new ArrayList<String>();
		tokens.addAll(SearchUtil.getTokensForIndexingOrQuery(Strings.nullToEmpty(fileName).toLowerCase(), 80));
		System.out.println("keywords list:"+tokens);
		obj.setKeyWords(tokens);
		//obj.setKeyWords("test");
		obj.setVotes(1);
		pm.makePersistent(obj);
		pm.close();
		
	}
	
	/**
	 * 
	 * @return searchresults page
	 */
	
	static PrivateKey privKey;
	static PublicKey pubKey;

	String keyString=null;
	@RequestMapping(value="/searchController", method=RequestMethod.GET)
	@ResponseBody public String searchresults(@RequestParam(value="name", required=false)String keyPhrase,HttpServletRequest req, HttpServletResponse resp) throws IOException, NoSuchAlgorithmException, NoSuchProviderException{
		Logger logger=Logger.getLogger("SearchController");
		//List<FileJdo> retVal=null;
		List<FileJdo> retVal=new ArrayList<FileJdo>();
		logger.info("searchController");
		HttpSession session = req.getSession(true);	
		HashMap<Integer,List<String>> hs=new HashMap<Integer,List<String>>();
//		if(session.getAttribute("key")==null)
//		{
			String keyString = StringUtils.deleteWhitespace(keyPhrase);
			System.out.println("keyPhrase  :: "+keyPhrase);
			String[] parts = keyPhrase.split(" ");
			
			
		    
			retVal=searchEntriesWithKeyWordTry1(keyString ,PMF.getPMF().getPersistenceManager());
			System.out.println("serach size"+ retVal.size() );
			if(retVal.size() == 0 ){
				
				
				for (String str : parts)
				{
				    System.out.println(str);
				    retVal=searchEntriesWithKeyWordTry1(str ,PMF.getPMF().getPersistenceManager());
				    if(retVal.size() != 0 )
				    	break;
				}
				
				
				
				
			}
			
			if(retVal.size() > 0 ){
			
				for(int i=0;i<retVal.size();i++){
					FileJdo cd= retVal.get(i);
					ArrayList<String> each=new ArrayList<String>();
					
					each.add(cd.getUserName());
					each.add(cd.getUserEmail());
					each.add(cd.getFileName());
					each.add(cd.getuploadFile());
					
					hs.put(i, each);
				}
				
			}
			
			
			
			System.out.println("retval size  ===  " + retVal.size());
			
			if(retVal.size() == 0)
				return new ObjectMapper().writeValueAsString("failed");
			
			return new ObjectMapper().writeValueAsString(hs);
			//resp.getWriter().println(new ObjectMapper().writeValueAsString(retVal) );
//		}
//		else{
//			logger.info("old Seesion");
//			logger.info(session.getAttribute("userid").toString());
//			return session.getAttribute("key").toString();		
//		}
	}
	
	@RequestMapping(value="/changeRole", method=RequestMethod.GET)
	@ResponseBody public void changeRole(@RequestParam(value="email", required=false)String email,HttpServletRequest req, HttpServletResponse resp) throws IOException{
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		keyString=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), email);
		UserDetailsJdo customer= pm.getObjectById(UserDetailsJdo.class, keyString);
		String role="";
		HttpSession session= req.getSession(true);
		if(customer != null){
			if(customer.getRole().equals("Admin")){
				customer.setRole("User");
				role="User";
			}
			else if(customer.getRole().equals("User"))
			{
				customer.setRole("Admin");
				SendEmail.reqAdminApprove(email,customer.getFirstname());
				role="Admin";
			}
				
		}
		System.out.println("change role");
		resp.getWriter().println(role);
		//return "sucess";

	}
	
	public static List <FileJdo> searchEntriesWithKeyWordTry1( String queryString , PersistenceManager pm  )
	{
		Logger log=Logger.getLogger("SearchController");
		List <FileJdo> result = null;
		
			StringBuffer queryBuffer = new StringBuffer();
			System.out.println("queryString -- "+queryString);
			//queryBuffer.append( "SELECT FROM " + SearchEntryProductData.class.getName() + " WHERE " );
			queryBuffer.append( "SELECT FROM " + FileJdo.class.getName() + " WHERE " );

			List<String>parametersForSearch=new ArrayList<String>();
			parametersForSearch.add(queryString);
			StringBuffer declareParametersBuffer = new StringBuffer();
			queryBuffer.append( "keyWords == param" + 0 );
			
			declareParametersBuffer.append( "String param" + 0);
			
			
			
			Query query = pm.newQuery( queryBuffer.toString() );

			query.declareParameters( declareParametersBuffer.toString() );
			//query.setRange(fromIndex, toIndex);
			query.setOrdering("votes desc");
			log.info("the query is"+queryBuffer.toString());
			log.info("the parameters are "+declareParametersBuffer.toString());
			StringBuffer paramsBuf=new StringBuffer();
			for(String str:parametersForSearch)
			{
				paramsBuf.append(str+" , ");
				System.out.println("str -- "+str);
			}
			log.info(paramsBuf.toString());
			try
				{
					result = (List <FileJdo>) query.executeWithArray( parametersForSearch.toArray() );
					result=(List <FileJdo>)pm.detachCopyAll(result);
					
					
					if(result==null)
					{
						log.info( "Result is null" );
						
					}
					else if(result!=null && result.size()==0)
					{
						log.info( "Result is zero" );
						
					}
					
					for(FileJdo file : result){
						log.info( "File Name --- "+file.getFileName() );
					}
					
					log.info( "Result for the given parameter  ===  " + result.size() );

				}
			catch ( DatastoreTimeoutException e )
				{
					log.severe( e.getMessage() );
					log.severe( "datastore timeout at: " + queryString );// +
																			// " - timestamp: "
																			// +
																			// discreteTimestamp);
				}
			catch ( DatastoreNeedIndexException e )
				{
					log.severe( e.getMessage() );
					log.severe( "datastore need index exception at: " + queryString );// +
																						// " - timestamp: "
																						// +
																						// discreteTimestamp);
				}
			
		
		
			return result;
	}
	
	
	
	@SuppressWarnings("unused")
	@RequestMapping(value="/validateKey", method=RequestMethod.GET)
	public void downloadFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException
	//@ResponseBody public void downloadFiles(@RequestParam(value="blobKey", required=false)String strBlobKey,HttpServletRequest req, HttpServletResponse resp) throws IOException
	{ 	
		filecount++;
		System.out.println("hi image");
		try{		
			String strBlobKey=req.getParameter("secureKey");
			
			
			
			
			String[] parts = strBlobKey.split("~");
			String sessionKey = parts[1];
			String keyString = parts[0];
			
			System.out.println("key -- "+ keyString);
			if(keyString==null)
				throw new Exception("blobKey parameter not specified!");
			BlobKey blobKey=new BlobKey(keyString);
			HttpSession session=req.getSession(true);
			BlobInfoFactory bif = new BlobInfoFactory();
			BlobInfo blobInfo = bif.loadBlobInfo(blobKey);
			PersistenceManager pm = PMF.getPMF().getPersistenceManager();
			String FileName = blobInfo.getFilename();
			if(FileName !=null)
			{
				FileName = FileName.replace(",", "");
				resp.setContentType(blobInfo.getContentType());
				resp.setHeader("Content-Disposition","attachment; filename="+FileName);
				
				System.out.println("File found.--- "+FileName);
				updateVotes(keyString);
				
				UserTrackJdo obj=new UserTrackJdo();
				obj.setEmail(session.getAttribute("userid").toString());
				obj.setUserName(session.getAttribute("userName").toString());
				obj.setDateDownloaded(new Date());
				obj.setIsDeleted(false);
				obj.setFileName(FileName);
				pm.makePersistent(obj);
				pm.close();
				
			}
			else{
				System.out.println("BlobDetail NOT found.");
			}
			
			//log.info("BlobDetail NOT found.");
			BlobstoreService blobStoreService=BlobstoreServiceFactory.getBlobstoreService();
			blobStoreService.serve(blobKey, resp);
		}
		catch(Exception e){
			System.out.println("Exception in BlobServeServlet : "+e);
		}
		finally{

		}	
	
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/sendKeytoMail", method=RequestMethod.POST)
	@ResponseBody public void sendKeytoMail(@RequestParam(value="fileName", required=false)String fileName,HttpServletRequest req, HttpServletResponse resp) throws IOException
	//public void recoverMail(HttpServletRequest req,HttpServletResponse resp) throws Exception
	{ 	
		
		//System.out.println("sending key for the fileName..........."+fileName);
		HttpSession session=req.getSession(true);
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		
		//System.out.println("sesssion: "+req.getSession().getId());
		
		
		String key=KeyFactory.createKeyString(FileJdo.class.getSimpleName(), fileName);
		FileJdo fileObj=pm.getObjectById(FileJdo.class, key);
		
		String keyString = fileObj.getImageId()+"~"+req.getSession().getId();
		//System.out.println(""+fileObj.getImageId() );
		
		System.out.println(" ***** key to send to mail ***** ::::  "+keyString);
		if(fileObj.getImageId() != null){
			SendEmail.keyEmail(session.getAttribute("userid").toString(), keyString);
		}
		
	}
	
	@RequestMapping(value="/reqAdmin", method=RequestMethod.GET)
	@ResponseBody public void reqAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		
		HttpSession session= req.getSession(true);
		String UserName = session.getAttribute("userName").toString();
		String UserEmail = session.getAttribute("userid").toString();
		
		keyString=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), UserEmail);
		UserDetailsJdo customer= pm.getObjectById(UserDetailsJdo.class, keyString);
		String role="";
		if(customer != null){
			
			customer.setIsreqAdmin("true");
			//session.setAttribute("reqAdmin","true");
			
			session.removeAttribute("reqAdmin");
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append( "SELECT FROM " + UserDetailsJdo.class.getName());
			Query q = pm.newQuery( queryBuffer.toString() );
			List<UserDetailsJdo> li=(List<UserDetailsJdo>) q.execute();
			System.out.println(" list size " + li.size());
			
			for(UserDetailsJdo cd:li)
			{
				if(cd.getRole().equals("Admin") ){
					SendEmail.reqAdmin(cd.getEmail().toString(),session.getAttribute("userName").toString());
				}
			}
		
			
				
		}
		System.out.println("change role");
		resp.getWriter().println(role);
		//return "sucess";

	}
	
	@RequestMapping(value="/validateOauth2Response.htm")
	public ModelAndView validateOauth2Response(@RequestParam(value="code",required=false)String code,@RequestParam(value="pageuri",required=false)String pageuri,HttpServletRequest req,HttpServletResponse res)
	{
		//srinivas code for cache authorization.
		ModelAndView mv=null;
		String nameSpaceObject = null;
		String pagerespuri = null;
//		if(pageuri != null && pageuri !="") 
//        {
//			Cookie cookie = VeroniqaCookieUtil.createNewCookie(req, res, "pagepathurl",pageuri ,365*24*60*60);
//			res.addCookie(cookie);
//        }
		
		try
		{		
//			ResourceBundle rb=ResourceBundle.getBundle("oauth");
//			String mode=EnvironmentUtil.getEnvironmentValue("AppMode").toLowerCase();
			//String projectMode="SOLESTRUCK";
			String oauthurl;
			String clientid="354506878931-gl8oib07ri39cvaronbm2lvg17slsabk.apps.googleusercontent.com";	
			String redirecturi="http://localhost:9999/validateOauth2Response.htm";
//			String versionString=req.getServerName().split("\\.")[0];
//			Integer version=-1;
//			/**
//			 * This is to support appspot versions since Oauth2 requires the full URL of the application to return the token.
//			 * To make oauth work in in non-default versions, add the full appspot uri in the oauth console : http://code.google.com/apis/console in the rediret uri block.
//			 * The oauth console can be opened with Developer@solestruck.com account.
//			 */
//			try
//				{version=Integer.parseInt(versionString);}
//			catch(Exception e)
//				{logger.info("Not parseable");version=-1;}
//			if(version!=-1)
//				redirecturi=redirecturi.replace("http://", "http://"+version+".");
////			String brandId=EnvironmentUtil.getEnvironmentValue("BrandId");
//			String remoteHost = req.getServerName();
//			String ip = req.getRemoteAddr();
//			if(remoteHost.contains("localhost")){
//				remoteHost = req.getServerName()+":"+req.getServerPort();
////				if(mode.toLowerCase().equals("dev"))
////					redirecturi=rb.getString("dev_"+req.getServerPort()+"_redirecturi");
////				if(mode.toLowerCase().equals("staging"))
////					redirecturi=rb.getString("staging_"+req.getServerPort()+"_redirecturi");
////				if(mode.toLowerCase().equals("live"))
//					redirecturi=rb.getString("live_"+req.getServerPort()+"_redirecturi");
//			}else{
			//https://accounts.google.com/o/oauth2/token?scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile&redirect_uri=http://localhost:9999/validateOauth2Response.htm&response_type=code&client_id=354506878931-gl8oib07ri39cvaronbm2lvg17slsabk.apps.googleusercontent.com
			//https://accounts.google.com/ServiceLogin?service=lso&passive=1209600&continue=https://accounts.google.com/o/oauth2/auth?response_type%3Dcode%26scope%3Dhttps://www.googleapis.com/auth/userinfo.email%2Bhttps://www.googleapis.com/auth/userinfo.profile%26access_type%3Doffline%26redirect_uri%3Dhttp://releasenotessolestruck.appspot.com/oauth2callback%26client_id%3D728966186895-ktlp2apcpvhjovb7m8shi56j9css0ruc.apps.googleusercontent.com%26hl%3Den-GB%26from_login%3D1%26as%3D14b07b00a1d5ee76&ltmpl=popup&shdf=CowBCxIRdGhpcmRQYXJ0eUxvZ29VcmwaAAwLEhV0aGlyZFBhcnR5RGlzcGxheU5hbWUaFnJlbGVhc2Vub3Rlc3NvbGVzdHJ1Y2sMCxIGZG9tYWluGhZyZWxlYXNlbm90ZXNzb2xlc3RydWNrDAsSFXRoaXJkUGFydHlEaXNwbGF5VHlwZRoHREVGQVVMVAwSA2xzbyIUCn1VOoauL7LIam5Pk-HSX0EFuGkoATIUw9y04cgkczYQpZZNNcWV8cXaZi8&sarp=1&scc=1
//				remoteHost = req.getServerName();
//			}
			logger.info("code:"+code);
			if(code==null)
			{						
			oauthurl="https://accounts.google.com/o/oauth2/auth";
			String redirectURL=oauthurl+"?scope=https://www.googleapis.com/auth/userinfo.email https://www.googleap" +
					"is.com/auth/userinfo.profile&redirect_uri="+redirecturi+"&response_type=code&client_id="+clientid;
			res.sendRedirect(redirectURL);
			}
			else
			{
			oauthurl="https://accounts.google.com/o/oauth2/token";
			String clientsecret="8RwVv37tQaYAl8t_X0taFCwn";
			String paramstr="code="+code+"&client_id="+clientid+"&client_secret="+clientsecret+"&redirect_uri="+redirecturi+"&grant_type=authorization_code";
			HashMap tokenresultMap=getMapAsResponseByCallService(oauthurl, paramstr,"POST");
			if(tokenresultMap==null||tokenresultMap.containsKey("error"))
			{
				res.sendRedirect("/");
			}
			logger.info("token result:"+tokenresultMap);
			String accesstoken=(String)tokenresultMap.get("access_token");
			String googleapiurl="https://www.googleapis.com/oauth2/v1/userinfo";
			googleapiurl+="?access_token="+accesstoken;
			HashMap userInfoMap=getMapAsResponseByCallService(googleapiurl, paramstr, "GET");
			if(userInfoMap==null)
				res.sendRedirect("/");
			String email="";
			String fname="";
			String lname="";
			String uniquepin="";
			String serviceProvider="Google";
			String profileImage="";
			String googlePlusLink="";
			email=(String) userInfoMap.get("email");
			fname=(String) userInfoMap.get("given_name");
			lname=(String) userInfoMap.get("family_name");
			uniquepin=(String) userInfoMap.get("id");
			if(userInfoMap.get("picture")!=null)
				profileImage=(String) userInfoMap.get("picture");
			if(userInfoMap.get("link")!=null)
				googlePlusLink=(String) userInfoMap.get("link");
			logger.info("user info result:"+userInfoMap);
			
//			String pageuricookie = VeroniqaCookieUtil.getCookieValue(req, "pagepathurl");
//			log.info("pageuricookie-->"+pageuricookie);
//			// cookie created
			
			if(email.indexOf("@a-cti.com")!=-1 || email.indexOf("@solestruck.com")!=-1)
			{
//				if( "clearAllFrontEndCache.htm".equals(pageuricookie))
		   		{
//					VeroniqaCookieUtil.deleteCookie(req, "pageuricookie");
					
//					MemcachedUtil.flushCache(MemcachedConstants.MENS_VENDOR_LIST);
//					MemcachedUtil.flushCache(MemcachedConstants.WOMENS_VENDOR_LIST);
//					MemcachedUtil.flushCache(MemcachedConstants.GLOBAL_NAVIGATION_MENU_NS);
//					MemcachedUtil.flushCache(MemcachedConstants.NEW_ARRIVALS);
//					MemcachedUtil.flushCache(MemcachedConstants.SALE_ITEMS);
//					MemcachedUtil.flushCache(MemcachedConstants.VINTAGE_ITEMS);
//					MemcachedUtil.flushCache(MemcachedConstants.CATEGORY_LISTING_VENDOR_NS);
//					MemcachedUtil.flushCache(MemcachedConstants.COMMON_JS_CSS);
//					MemcachedUtil.flushCache(MemcachedConstants.PAGE_CACHE);
//					//FAE
//					MemcachedUtil.flushCache(MemcachedConstants.HOME_PAGE);
//					//
//					MemcachedUtil.flushCache(MemcachedConstants.LOOK_BOOK);
//					MemcachedUtil.flushCache(MemcachedConstants.IDP_NAME_SPACE);
//					MemcachedUtil.flushCache(MemcachedConstants.FINAL_SALE);
					
					//MemcachedUtil.flushCompleteCache();
//					clearAllCache();
//					String msg = "Cache Cleared ...";
//				    mv = new ModelAndView("clearAllFrontEndCache");
//				    mv.addObject("msg",msg);
//				    mv.addObject("urihistry", pageuricookie);
		   		}
				
			}
			else
			   {
//					logger.info("logging in from un authorized email id not allowed ");
//				   String msg = "Logging in using unauthorized email id is not allowed, login either using solestruck or a-cti email ids";
//				   mv = new ModelAndView("clearAllFrontEndCache");
//				   mv.addObject("msg",msg);
//				   mv.addObject("urihistry", pageuricookie);
			   }
			//need to handle failure cases
			
			}			
		}
		catch(Exception e)
		{
			for(StackTraceElement ele:e.getStackTrace())
				logger.warning(ele.toString());
		}		
		return mv;
		
	}
	
	private HashMap getMapAsResponseByCallService(String url,String paramstr,String method)
	{
		HashMap responseMap=null;
		try
		{
		   URL myurl = new URL(url);
	       HttpURLConnection connection = (HttpURLConnection) myurl.openConnection();
	       logger.info("url connection set");
	       connection.setDoOutput(true);
	       connection.setRequestMethod(method);
	       connection.setConnectTimeout (300000); //300s = 5mins
	       connection.setReadTimeout (300000); //300s = 5mins
	      
//	       if("POST".equals(method))
//	       {
//	    	   OutputStream writer = new OutputStreamWriter(connection.getOutputStream());
//	    	   logger.info("*******Parameter****"+paramstr);
//		       try {
//		    	   	writer.write(paramstr);
//		       		} catch (IOException e1) {
//		       			e1.printStackTrace();
//		       }
//		       try {
//		    	   writer.close();
//		       } catch (IOException e1) { 
//		       e1.printStackTrace();
//		       }
//		       
//	       }
	      
	       String responseString="";
	       if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) 
	       {
		    	  String inputLine;
		    	  BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
		    	  while ((inputLine = reader.readLine()) != null) {
		    		  responseString+=inputLine;
		    	   }
		    	  reader.close();
		    	   if(responseString.length() <= 0)
		    	   {
		    	   throw new Exception(responseString);
		    	   }
	    	  
	    	 }
	       else 
	    	 {
	    	   logger.warning("exception :"+connection.getResponseCode()+"");
	    	     // Server returned HTTP error code.
	    	     throw new Exception(connection.getResponseCode()+"");
	    	 }
	       responseMap = new ObjectMapper().readValue(responseString, HashMap.class);
	       logger.info("Response :"+responseString);
		}
		catch(Exception e)
		{
			responseMap=new HashMap();
			responseMap.put("exception",e.getMessage());
		}
		
		return responseMap;

	}
	
	
	public void sendMailtoUsers(){

		System.out.println("/sendMailtoUsers");
		ModelAndView m=new ModelAndView("fileinfo");
		List<FileJdo> retVal= new ArrayList<FileJdo>();
		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
//		HashMap<Integer,List<String>> hs=new HashMap<Integer,List<String>>();
		int i=0;
		Query q=pm.newQuery(FileJdo.class);
		@SuppressWarnings("unchecked")
		List<FileJdo> li=(List<FileJdo>) q.execute();
		
		String htmlString ="<br/> files  ";
		
		SendEmail.sendCronEmail("mail2gstech@gmail.com","Gs","cron test");
//		for(FileJdo cd:li)
//		{			
//			System.out.println("filejdo "+ cd.getFileName());
//			htmlString=htmlString.concat("<br/>"+cd.getFileName()+"<br/>");
//			
//			
//		}
//		
//		UserDetailsJdo customer= pm.getObjectById(UserDetailsJdo.class, keyString);
//		if(customer != null){
//			
//			Query q1 = pm.newQuery(UserDetailsJdo.class);
//			List<UserDetailsJdo> li1=(List<UserDetailsJdo>) q1.execute();
//			System.out.println(" list size for send mail" + li.size());
//			for(UserDetailsJdo cd1:li1)
//			{
//				if(cd1.getRole().equals("User") ){
//					SendEmail.sendCronEmail(cd1.getEmail().toString(),cd1.getFirstname().toString(),htmlString);
//					
//				}
//			}
//				
//		}
	
	
	}
}

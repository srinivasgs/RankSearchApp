package com.GoogleAppEngine;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.GoogleAppEngine.jdo.PMF;
import com.GoogleAppEngine.jdo.UserDetailsJdo;
import com.GoogleAppEngine.useractivity.Login;
import com.GoogleAppEngine.useractivity.Register;
import com.GoogleAppEngine.utilities.SendEmail;
import com.GoogleAppEngine.utilities.TextEncription;
import com.GoogleAppEngine.controller.UserController;
import com.google.appengine.api.datastore.KeyFactory;

@Controller
public class UserController 
{
	PersistenceManager persistenceManager = null;
	Logger logger=Logger.getLogger(UserController.class.getName());
	JSONParser parser=new JSONParser();
	JSONObject responseJson=null;
	/**
	 * This is mapping for home and index URL.
	 */
	@RequestMapping(value={"/", "/home"})
	public String indexURL()
	{
		return "home";
	}
	/**
	 * This is mapping for admin URL.
	 */
	@RequestMapping(value="/admin", method=RequestMethod.GET)
	public String adminURL()
	{
		return "admin";
	}
	
	/**
	 * @param timezoneData 
	 * @return status of admin Action(update & delete)
	 * This is mapping for admin action.(update and deletion)
	 */
//	@SuppressWarnings("unchecked")
//	@RequestMapping(value="/admin", method=RequestMethod.POST)
//	public @ResponseBody String adminAction(@RequestBody String timezoneRequestData)
//	{
//		logger.info("inside admin action.");
//		try 
//		{
//			JSONObject timezoneData=(JSONObject) parser.parse(timezoneRequestData);
//			if(timezoneData.get("operationRequired").toString().equals("update"))
//			{
//				logger.info("inside update.");
//				JSONObject oldTimezoneData=(JSONObject) timezoneData.get("oldtimezonedata");
//				JSONObject newTimezoneData=(JSONObject) timezoneData.get("newtimezonedata");
//				UpdateData updateData=new UpdateData();
//				String update=updateData.changeData(oldTimezoneData, newTimezoneData);
//				if(update.equals("success")){
//					responseJson=new JSONObject();
//					responseJson.put("status", "success");
//					return responseJson.toJSONString();
//				}
//				else{
//					responseJson=new JSONObject();
//					responseJson.put("status", "failed");
//					return responseJson.toJSONString();
//				}
//				
//			}
//			else if(timezoneData.get("operationRequired").toString().equals("delete"))
//			{
//				logger.info("inside delete.");
//				JSONObject TimezoneData=(JSONObject) timezoneData.get("timezoneData");
//				DeleteData deleteData=new DeleteData();
//				String delete=deleteData.deleteData(TimezoneData);
//				if(delete.equals("success")){
//					responseJson=new JSONObject();
//					responseJson.put("status", "success");
//					return responseJson.toJSONString();
//				}
//				else{
//					responseJson=new JSONObject();
//					responseJson.put("status", "failed");
//					return responseJson.toJSONString();
//				}
//				
//			}
//			else{
//				responseJson=new JSONObject();
//				responseJson.put("status", "failed");
//				responseJson.put("msg", "required operation is not mentioned");
//				return responseJson.toJSONString();
//			}
//		} catch (Exception e) 
//		{
//			e.printStackTrace();
//			logger.severe("exception in admin Action.");
//			responseJson=new JSONObject();
//			responseJson.put("status", "failed");
//			responseJson.put("msg", "please follow required format");
//			return responseJson.toJSONString();
//		}
//		
//	}
	
	/**
	 * 
	 * @param req
	 * @param resp
	 * @return login page
	 */
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String loginURL(HttpServletRequest req, HttpServletResponse resp)
	{
		return "login";
	}
	/**
	 * 
	 * @param req
	 * @param resp
	 * @return register page
	 */
			
	@RequestMapping(value="/register", method=RequestMethod.GET)
	public String registerURL(HttpServletRequest req, HttpServletResponse resp)
	{
		return "register";
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @return login page
	 */
	@RequestMapping(value="/logout", method=RequestMethod.GET)
	public String logoutURL(HttpServletRequest request, HttpServletResponse response)
	{
		request.getSession().invalidate();
		return "login";
	}
	
	/**
	 * 
	 * @param req
	 * @param loginData
	 * @return status of login with key
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/login", method=RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public @ResponseBody String loginAction(HttpServletRequest req, @RequestBody String loginData)
	{
		logger.info("login Request with: "+loginData);
		try
		{
			JSONObject loginDataJsonObject=(JSONObject) parser.parse(loginData);
			Login login=new Login();
			String key=login.login(req.getSession(), loginDataJsonObject.get("userid").toString(), loginDataJsonObject.get("password").toString());
			responseJson=new JSONObject();
			if(key!=null){
				responseJson.put("status", "success");
				responseJson.put("key", key);
			}
			else{
				responseJson.put("status", "failed");
				responseJson.put("key", null);
				responseJson.put("error", "Your are not registered. Please register to use file secure");
			}
			logger.info(responseJson.toJSONString());
			return responseJson.toJSONString();
		}
		catch(Exception e)
		{
			responseJson=new JSONObject();
			responseJson.put("status", "failed");
			responseJson.put("key", null);
			responseJson.put("error", "Your data is not in correct format");
			return responseJson.toJSONString();
		}
	}
	
	/**
	 * 
	 * @param req
	 * @param userData
	 * @return status of registration with key
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public @ResponseBody String registerAction(HttpServletRequest req, @RequestBody String userData)
	{
		System.out.print("key --- "+userData);
		try
		{
		JSONObject userDataJsonObject=(JSONObject) parser.parse(userData);
		Register register=new Register();
		if(userDataJsonObject.get("userid").toString().matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[-A-Za-z0-9]+(\\.[A-Za-z]{1,})+$")&&userDataJsonObject.get("password").toString().length()>=6)
		{
			
			//System.out.println(userDataJsonObject.get("userid").toString(),userDataJsonObject.get("userid").toString(),userDataJsonObject.get("userid").toString(),userDataJsonObject.get("userid").toString(),userDataJsonObject.get("userid").toString());
			
			System.out.println("Lastname --- "+userDataJsonObject.get("lname").toString());
			System.out.println("FirstName --- "+userDataJsonObject.get("bdate").toString());
			System.out.println("Mobile --- "+userDataJsonObject.get("mobnumber").toString());
			System.out.println("gender --- "+userDataJsonObject.get("gender").toString());
			if(!register.alreadyRegisterd(userDataJsonObject.get("userid").toString()))
			{
				String key=register.addUser(req.getSession(), userDataJsonObject.get("userid").toString(), userDataJsonObject.get("password").toString(),userDataJsonObject.get("fname").toString(),userDataJsonObject.get("lname").toString(),userDataJsonObject.get("mobnumber").toString(),userDataJsonObject.get("gender").toString(),userDataJsonObject.get("bdate").toString());
				System.out.print("key --- "+key);
				
				responseJson=new JSONObject();
				if(key!=null)
				{
					responseJson.put("status", "success");
					responseJson.put("key", key);
					return responseJson.toJSONString();
				}
				else
				{
					responseJson.put("status", "failed");
					responseJson.put("key", null);
					responseJson.put("error", "Key should not be null");
					return responseJson.toJSONString();
				}
				
			}
			else
			{
				
				responseJson=new JSONObject();
				responseJson.put("status", "failed");
				responseJson.put("key", null);
				responseJson.put("error", "User ID is already registered");
				return responseJson.toJSONString();
				
			}
		}
		else
		{
			responseJson=new JSONObject();
			responseJson.put("status", "failed");
			responseJson.put("key", null);
			responseJson.put("error", "User ID must be email, and Password must have atleast 6 character");
			return responseJson.toJSONString();
		}
		}
		catch(Exception e)
		{
			responseJson=new JSONObject();
			responseJson.put("status", "failed");
			responseJson.put("key", null);
			responseJson.put("error", "Your data is not in correct format");
			return responseJson.toJSONString();
		}
	}
	
	/**
	 * 
	 * @return recovery page
	 */
	@RequestMapping(value="/recover/initiate", method=RequestMethod.GET)
	public String recoverIntiateURL()
	{
		return "recovery";
	}
	/**
	 * 
	 * @param req(user email)
	 * @param resp
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/recover/initiate", method=RequestMethod.POST)
	public @ResponseBody String recoverMail(HttpServletRequest req,@RequestBody String recoveryJson)
	{
		try
		{
			JSONObject recoveryJsonObject=(JSONObject) parser.parse(recoveryJson);
			String userid=recoveryJsonObject.get("userid").toString();
			logger.info("UserId for password recovery"+userid);
			String key=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), userid);
			UserDetailsJdo customerJDO=persistenceManager.getObjectById(UserDetailsJdo.class, key);
			if(!customerJDO.isForgot())
			{
				SendEmail.email(userid, customerJDO.getPassword());
				customerJDO.setForgot(true);
				responseJson=new JSONObject();
				responseJson.put("msg", "Email sent successfully .");
				responseJson.put("status", "success");
				return responseJson.toJSONString();
			}
			else
			{
				responseJson=new JSONObject();
				responseJson.put("msg", "email already sent. Please check your email");
				responseJson.put("status", "failed");
				return responseJson.toJSONString();
			}
		}
		catch(Exception e)
		{
			responseJson=new JSONObject();
			responseJson.put("msg", "your email is not yet registered");
			responseJson.put("status", "failed");
			return responseJson.toJSONString();
		}
	}
	/**
	 * 
	 * @param req
	 * @param resp
	 * @param model
	 * @return changePassword page if passed else login page with error message.
	 */
	@RequestMapping(value="/recover", params={"userid", "tpass"}, method=RequestMethod.GET)
	public ModelAndView recoverPass(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			persistenceManager =PMF.getPMF().getPersistenceManager();
			String key=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), req.getParameter("userid"));
			UserDetailsJdo customerObj=persistenceManager.getObjectById(UserDetailsJdo.class, key);
			String tempPass=req.getParameter("tpass");
			if(customerObj.getEmail().equalsIgnoreCase(req.getParameter("userid"))&&customerObj.getPassword().equals(tempPass))
			{
				req.getSession().setAttribute("recover", "ok");
				req.getSession().setAttribute("userid", req.getParameter("userid"));
				return new ModelAndView("changePassword", "userid", req.getParameter("userid"));
			}
			else
			{
				return new ModelAndView("login", "recoverfail", "your creditial are wrong. Please try again");
			}
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			return new ModelAndView("login", "recoverfail", "your creditial are wrong. Please try again");

		}
	}
	/**
	 * 
	 * @param req(user email)
	 * @param resp
	 * @return user found/notFound
	 */
	@RequestMapping(value="/recover/searchuser",  method=RequestMethod.POST)
	public @ResponseBody String searchUser(HttpServletRequest req, HttpServletResponse resp)
	{
		logger.info(req.getParameter("userid"));
		try
		{
			persistenceManager =PMF.getPMF().getPersistenceManager();
			System.out.println(KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), req.getParameter("userid")));
			
			String key=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), req.getParameter("userid"));
			persistenceManager.getObjectById(UserDetailsJdo.class, key);
			return "found";
		}
		catch(Exception e)
		{
			return "notFound";
		}
	}
	@RequestMapping(value="/passwordchange",  method=RequestMethod.GET)
	public void changePasswordURL(HttpServletRequest req, HttpServletResponse resp)
	{
		try 
		{
			resp.sendRedirect("/login");
		} 
		catch (IOException e)
		{
			logger.warning(e.getMessage());
		}
	}
	/**
	 * 
	 * @param req
	 * @param resp
	 * @return Model and view after changing password
	 */
	@RequestMapping(value="/passwordchange",  method=RequestMethod.POST)
	public ModelAndView changePassword(HttpServletRequest req, HttpServletResponse resp)
	{
		System.out.println("passwordchange");
		try
		{
			String password=TextEncription.sha1(req.getParameter("pass"));
			String userid=(String) req.getSession().getAttribute("userid");
			logger.info("User Id: "+userid+" and Password: "+password);
			PersistenceManager pm = PMF.getPMF().getPersistenceManager();
			String key=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), userid);
			UserDetailsJdo customerJDO=pm.getObjectById(UserDetailsJdo.class, key);
			if(customerJDO.isForgot())
			{
				if(!customerJDO.getPassword().equals(password))
				{
					customerJDO.setPassword(password);
					customerJDO.setForgot(false);
					req.getSession().invalidate();
					return new ModelAndView("login","recovermsg", "Password changed success fully. Please login..");
				}
				else
				{
					return new ModelAndView("changePassword","changefail", "You are using your old password");
				}
			}
			else
			{
				
				return new ModelAndView("login","recoverfail", "You haven't yet applied for forget password. Or you have already changed it.");
			}
		}
		catch(Exception e)
		{
			req.getSession().invalidate();
			return new ModelAndView("login","recoverfail", "sorry!! recovery failed. Please try again. ");
		}
		
	}
	
	
	/**
	 * 
	 * @param req
	 * @param resp
	 * @return Model and view after changing password
	 */
	@RequestMapping(value="/changePasswordRequestAction",  method=RequestMethod.POST)
	public ModelAndView changePasswordRequestAction(HttpServletRequest req, HttpServletResponse resp)
	{
		System.out.println("changePasswordRequestAction");
		try
		{
			logger.info("User Id: "+req.getSession().getAttribute("userid")+" and Old Password: "+req.getParameter("oldPass")+" and Old Password: "+req.getParameter("newPass"));
			
			String userid=(String) req.getSession().getAttribute("userid");
			String oldPassword=TextEncription.sha1(req.getParameter("oldPass"));
			String newPassword=TextEncription.sha1(req.getParameter("newPass"));
			
			logger.info("User Id: "+userid+" and Old Password: "+oldPassword+" and Old Password: "+newPassword);
			
			PersistenceManager pm = PMF.getPMF().getPersistenceManager();
			String key=KeyFactory.createKeyString(UserDetailsJdo.class.getSimpleName(), userid);
			UserDetailsJdo customerJDO=pm.getObjectById(UserDetailsJdo.class, key);
			if(customerJDO.getPassword().equals(oldPassword))
			{
					customerJDO.setPassword(newPassword);
					//customerJDO.setForgot(false);
					req.getSession().invalidate();
					pm.makePersistent(customerJDO);
					System.out.println("password changed");
					return new ModelAndView("login","recovermsg", "Password changed success fully. Please login..");
			}
			else
			{
				System.out.println("old password wrong");
					return new ModelAndView("changePasswordRequest","changefail", "You are entered incorrect old password");
			}
			
		}
		catch(Exception e)
		{
			System.out.println("exception"+e);
			req.getSession().invalidate();
			return new ModelAndView("login","recoverfail", "sorry!! change password failed. Please try again. ");
		}
		
	}
	
	
}

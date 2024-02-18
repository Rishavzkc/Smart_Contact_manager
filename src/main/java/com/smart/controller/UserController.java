package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	
	private Logger logger = LoggerFactory.getLogger(UserController.class);

	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		
		logger.info("USERNAME "+userName);
		
				//get the user using usernamne(Email)		
		
		User user = userRepository.getUserByUserName(userName);
		
		logger.info("USER "+user);
		
		model.addAttribute("user",user);
		
	
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,HttpSession session) {
		
		try {
			
			
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
	
		
		
		//processing and uploading file..
		
		if(file.isEmpty())
		{
			//if the file is empty then try our message
			System.out.println("File is empty");
			contact.setImage("contact.jpg");
		
		}
		else {
			//file the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			
			File saveFile=new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
			
		}
		
		
		user.getContacts().add(contact);
		
		contact.setUser(user);	
		
		
		
		this.userRepository.save(user);		
		
		logger.info("DATA "+contact);
	
		logger.info("Added to data base...........");
		
		
		//message success.......
		session.setAttribute("message", new Message("Your contact is added !! Add more..", "success") );
		
		}catch (Exception e) {		
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
		//message error
			session.setAttribute("message", new Message("Some went wrong !! Try again..", "danger") );
			
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		//contact ki list ko bhejni hai
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		//Pageable have two below information
		//currentPage-page
		//Contact Per page - 5
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal){
		
		System.out.println("CID" +cId);
	Optional<Contact> contactOptional=this.contactRepository.findById(cId);
	Contact contact=contactOptional.get();

	//check vaild user can his access own saved contact
String userName =principal.getName();
User user =  this.userRepository.getUserByUserName(userName);

if(user.getId()==contact.getUser().getId())
	{
		model.addAttribute("contact",contact);
	model.addAttribute("title", contact.getName());
	}
	return "normal/contact_detail";
	}



	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model, HttpSession session,
	Principal principle ){
	
		System.out.println(" CID "+ cId);
		
		Contact contact =  this.contactRepository.findById(cId).get();
	

//check... Assignment Done


System.out.println("Contact" +contact.getcId());

User user=this.userRepository.getUserByUserName(principle.getName());
user.getContacts().remove(contact);

this.userRepository.save(user);

// 

session.setAttribute("message", new Message("Contact deleted sucessfully....", "success"));

 return "redirect:/user/show-contacts/0";
	}
	

//open update from handler
@PostMapping("/update-contact/{cid}")
public String updateForm(@PathVariable("cid") Integer cid,Model m){

	m.addAttribute("title", "Update Contact");
	
	Contact contact=this.contactRepository.findById(cid).get();
	m.addAttribute("contact", contact);
	return "normal/update_form";
}

//update contact handler
@RequestMapping(value="/process-update",method = RequestMethod.POST)
public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
 Model m, HttpSession session, Principal principal){
	
	try{

//old contact details
Contact oldcontactDetail =this.contactRepository.findById(contact.getcId()).get();
		
//image
		if(!file.isEmpty()){
			//file work
			//rewrite
//delete old photo
File saveFile=new ClassPathResource("static/img").getFile();

//update new photo

File deleteFile=new ClassPathResource("static/img").getFile();
File file1 =new File(deleteFile, oldcontactDetail.getImage());
file1.delete();

Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	
Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

contact.setImage(file.getOriginalFilename());

}else {
			contact.setImage(oldcontactDetail.getImage());
		}
User user =userRepository.getUserByUserName(principal.getName());
contact.setUser(user);
		
this.contactRepository.save(contact);

session.setAttribute("message", new Message("Your contact is updated....", "success"));

	}catch(Exception e){
		e.printStackTrace();
	}
logger.info("CONTACT NAME" +contact.getName());
logger.info("CONTACT ID" +contact.getcId());

	
	return "redirect:/user/"+contact.getcId()+"/contact";
}


//your profile handler
@GetMapping("/profile")
public String yourProfile(Model model){
model.addAttribute("title", "Profile Page");
return "normal/profile";
}


//open setting handler
	@GetMapping("/settings")
	public String openSettings(){
		return "normal/settings";
	}
	
//change password handler
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session){
	
		System.out.println("OLD PASSWORD " +oldPassword);
		System.out.println("NEW PASSWORD " +newPassword);
		
		String userName =principal.getName();
		User currentUser =this.userRepository.getUserByUserName(userName);
		
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword()))
		{
			//change the password
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
		
session.setAttribute("message", new Message("Your password is successfully changed....", "success"));

		}else {
			//error..
			session.setAttribute("message", new Message("Please Enter correct old password !!", "danger"));
			 return "redirect:/user/settings";
		}
		 return "redirect:/user/index";
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws RazorpayException {
		//System.out.println("Order Function Executed ");
		System.out.println(data);
		
		//extract data from map
	  int amt=Integer.parseInt(  data.get("amount").toString());
		var client= new RazorpayClient("rzp_test_eUW9vGwB7CyIpV"
				, "manzvweYu8WFISuIBKs3jA99"
				); 
	   
		JSONObject ob= new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_234524");
		
		//creating and save the above order
		Order order =client.Orders.create(ob);
		System.out.println(order);
		
		//we can store the above order details in database
		//saving data....
	MyOrder myOrder=	new MyOrder();
	
	myOrder.setAmount(order.get("amount")+"");
	myOrder.setOrderId(order.get("id"));
	myOrder.setPaymentId(null);
	myOrder.setStatus("created");
	myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
	myOrder.setReceipt(order.get("receipt"));
	
	this.myOrderRepository.save(myOrder);
	
	return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object>data){
		
	MyOrder myorder=this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
	myorder.setPaymentId(data.get("payment_id").toString());
	myorder.setStatus(data.get("status").toString());
	
	this.myOrderRepository.save(myorder);

	System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
 
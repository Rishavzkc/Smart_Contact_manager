console.log("this is script file");

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {
		//true
		//close the sidebar
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {
		//false
		//open the sidebar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

//search
const search = () => {

	//console.log("searching....");

	let query = $("#search-input").val();

	if (query == " ") {
		$(".search-result").hide();
	} else {

		//search
		console.log(query);

		//sending request to server

		let url = `http://localhost:8284/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json();
			}).then((data) => {
				//data..........
				//console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name}  </a>`
				});

				text += `</div>`;

				$(".search-result").html(text);

				$(".search-result").show();

			});
	}
};


//first request- to server to create order

const paymentStart=() =>{
console.log("payment started...")
let amount =$("#payment_field").val();
console.log(amount);
if(amount =='' || amount == null){
//	alert("amount is required !!");
	swal("Failed !!","Amount is required !!","error")
return;
}

//Code..
//Ajax is used to send request to server to create order -Jquery

$.ajax(
	{
	   url:"/user/create_order",
       data:JSON.stringify({amount: amount, info:'order_request'}),
	   contentType: "application/json",
	   type:"POST", 
	   dataType: "json",
	   success:function(response){
	//invoke when success
	console.log(response)
	if(response.status =="created"){
		//open payment form 
		let options={
			key:"rzp_test_eUW9vGwB7CyIpV",
			amount: response.amount,
			currency:"INR",
			name: "Smart Contact Manager",
			description: "Donation",
			image:"https://cdn.vectorstock.com/i/1000x1000/37/81/payment-icon-vector-5783781.webp",
			order_id:response.id,
			handler:function(response){
				console.log(response.razorpay_payment_id)
				console.log(response.razorpay_order_id)
				console.log(response.razorpay_signature)
				console.log('payment successful !!')
				//alert("congrates !! Payment successfull !!")

				updatePaymentOnServer(
					response.razorpay_payment_id,
					response.razorpay_order_id,
					'paid'
					);

				
			},
			prefill: { 
				name: "", 
				email: "",
				contact: "" 
			}, 
			notes: {
				address: "Smart Contact Manager"
			},
			theme: {
				color: "#3399cc"
			}, 
	};
	let rzp =new Razorpay(options);
	rzp.on('payment.failed', function (response){
		console.log(response.error.code);
		console.log(response.error.description);
		console.log(response.error.source);
		console.log(response.error.step);
		console.log(response.error.reason);
		console.log(response.error.metadata.order_id);
		console.log(response.error.metadata.payment_id);
		//alert("Oops  payment failed !! ")
		swal("Failed !!","Oops payment failed !!","error")
	});
	rzp.open(); 

}
	   },
	   error:function(error){
		//invoke when error occured
		console.log(error)
		alert("something went wrong !!")
	   }
	})


};

function updatePaymentOnServer(payment_id,order_id,status)
{
	$.ajax({
		url:"/user/update_order",
       data: JSON.stringify({payment_id: payment_id,order_id:order_id, status:status}),
	   contentType: "application/json",
	   type:"POST", 
	   dataType: "json",
	   success:function(response){
		swal("Good Job !!","congrates !! Payment successfull !!","success");
	   },
	   error:function(error){
		swal("Failed !!","Your payment is successful, but we did not get on server, we wiil contact you ","error");
	   }
	});
}


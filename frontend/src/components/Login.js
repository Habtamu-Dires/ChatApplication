import React, { useState, useEffect } from "react";
import SingUp from "./SignUp";

function Login(props){
   const [user, setUser] = useState(null);
   const[type, setType] = useState("Login");    

   const login = () => {      
   fetch("http://localhost:8080/api/v1/auth/login",{
      method: "POST",
      body: JSON.stringify(user),
      credentials: "same-origin", 
      headers: {
         "Content-Type": "application/json",
      },
   })
   .then(res =>res.json())
   .then(apiRes => {
      console.log(apiRes)
      if(apiRes.success){
         const {username, jwtToken} = apiRes.data;
         localStorage.setItem("jwtToken",jwtToken);
         localStorage.setItem("username", username);
         props.setAuthUser({username, jwtToken});
      }
             
   })
   .catch(err => console.log('error ' + err))      
   }


    return(
      <div>
         {type === "Login" && <div className="signUp">
         <label style={{fontSize: '2em', fontWeight: 'bold'}}>Login</label>
         <input placeholder="UserName" onChange={(event) => {
            setUser({...user, "username": event.target.value})
         }}/>  

         <input placeholder="Password"  type="password"
            onChange={(event) => {
               setUser({...user, "password": event.target.value})
         }}/>  
        
        <button onClick={login}>Login</button>
        <a href="#" onClick={() =>setType("Register")}>Register</a>
      </div>}
      {type === "Register" && 
         <SingUp setAuthUser={props.setAuthUser}/>
      }  
      </div>
      
    );
}

export default Login;
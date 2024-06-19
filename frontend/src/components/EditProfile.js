import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function EditProfile(props){

    const[userProfile, setUserProfile] = useState(null);
    const navigate = useNavigate();

    //for basic authentication encode credentials
    function encodeCredentials(username, password) {
        return btoa(`${username}:${password}`)
    }

    const fetchUserProfile = () => {
        fetch(`http://localhost:8080/api/v1/users/profile/${props.authUser.username}`,{
            headers: {
                "Authorization": `Basic ${props.encodedCredentials}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){
                console.log("user dto " + apiRes.data.username)
                setUserProfile(apiRes.data);
            } else{
                alert(apiRes.message)
            }
        })
        .catch(err => console.log(err));
    }

    useEffect(()=>{
        fetchUserProfile();
    },[])

    const updateUserProfile = () => {
        const profileUpdateDto = {
            'oldUsername': props.authUser.username,
            'newUsername': userProfile.username,
            'password': userProfile.password,
            'firstName': userProfile.firstName,
            'lastName': userProfile.lastName,
            'phoneNumber': userProfile.phoneNumber
        }

        fetch(`http://localhost:8080/api/v1/users/profile-update`,{
            method: "PUT",
            body: JSON.stringify(profileUpdateDto),
            credentials: "same-origin", 
            headers: {
              "Authorization": `Basic ${props.encodedCredentials}`,
              "Content-Type": "application/json"
            },
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){
                console.log(apiRes);
                console.log(apiRes.data.newUsername);
                
               const username = apiRes.data.newUsername;
               const password = apiRes.data.password;
               console.log("username " + username);
               console.log("password " + password)
               props.setAuthUser({username, password});
               const encodedCredentials = encodeCredentials(username, password);
               props.setEncodedCredentials(encodedCredentials);

               navigate('/');
            } else{
                alert(apiRes.message);
            }
        })
        .catch(err => console.log(err));
    }
    return(
        <> {userProfile !== null && 
        <div className="signUp">
           <label>First Name</label>
           <input 
              placeholder="Frist Name"
              value={userProfile.firstName} 
              onChange={(event) => {
                  setUserProfile({...userProfile, firstName: event.target.value})
              }}
           /> 
            <label>Last Name</label>
           <input
               placeholder="Last Name" 
               value={userProfile.lastName}
               onChange={(event) => {
                  setUserProfile({...userProfile, lastName: event.target.value})
               }}
           /> 
            <label>UserName</label>
           <input 
              placeholder="username" 
              value={userProfile.username}
              onChange={(event) => {
                  setUserProfile({...userProfile, username: event.target.value})
              }}
           />  
           <label>Password</label>
            <input 
              placeholder="Password" 
              type="password"
              onChange={(event) => {
              setUserProfile({...userProfile, password: event.target.value})
           }}/>  
            <label>Phone Number </label>
           <input 
              placeholder="PhoneNumber" 
              value={userProfile.phoneNumber}
              onChange={(event) => {
              setUserProfile({...userProfile, phoneNumber: event.target.value})
           }}/>  
            <button onClick={()=> updateUserProfile()}>Upadate</button>
        </div> }
        { userProfile === null && 
            <div>
                <p>Loading ........</p>
            </div>
        } 
        </>
    );
}

export default EditProfile;
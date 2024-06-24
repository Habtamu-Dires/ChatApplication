import React, { useEffect, useState } from "react";
import Login from "./Login";
import ChatRoom from "./ChattRoom";
import { Route, Routes } from "react-router-dom";
import EditProfile from "./EditProfile";

function Home () {

    const [authUser, setAuthUser] = useState(null);

    // function encodeCredentials(username, password) {
    //     return btoa(`${username}:${password}`)
    // }
    console.log("The localstorage item")
    console.log( localStorage.getItem("username"));

    useEffect(()=>{
        if(localStorage.getItem("username") !== null 
            && localStorage.getItem('jwtToken') !== null){
            const username = localStorage.getItem("username");
            const jwtToken = localStorage.getItem("jwtToken");
            setAuthUser({username,jwtToken});
        }
    }, [])

    const Main =() =>{
        return(
            <div>
                {(authUser === null)&& 
                   <Login setAuthUser={setAuthUser}/> 
                }
                {(authUser !== null) &&
                    <ChatRoom setAuthUser={setAuthUser} authUser={authUser} />
                }
            </div>
        )
    }
    return (
        <Routes>
            <Route path="/" element={<Main/>}></Route>
            <Route path="/edit-profile"
               element={<EditProfile  authUser={authUser} setAuthUser={setAuthUser}/>}>                
            </Route>
        </Routes>        
    )

}

export default Home; 
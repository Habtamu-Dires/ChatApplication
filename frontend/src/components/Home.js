import React, { useEffect, useState } from "react";
import Login from "./Login";
import ChatRoom from "./ChattRoom";
import { Route, Routes } from "react-router-dom";
import EditProfile from "./EditProfile";

function Home () {

    const [authUser, setAuthUser] = useState(null);
    const [encodedCredentials, setEncodedCredentials] = useState(null);

    function encodeCredentials(username, password) {
        return btoa(`${username}:${password}`)
    }

    useEffect(()=>{
        if(authUser !== null){
            //for basic authentication encode credentials
        
        const encodedCreds = encodeCredentials(
            authUser.username, authUser.password
        );
            setEncodedCredentials(encodedCreds);
        }
    }, [authUser])

    const Main =() =>{
        return(
            <div>
                {(authUser === null)&& 
                <Login setAuthUser={setAuthUser}/> 
               // <ChatRoom setAuthUser={setAuthUser} authUser={authUser}/>
                }
                {(authUser !== null && encodedCredentials !== null) &&
                    <ChatRoom setAuthUser={setAuthUser} authUser={authUser}
                        setEncodedCredentials={setEncodedCredentials}
                        encodedCredentials={encodedCredentials}
                    />
                }
            </div>
        )
    }
    return (
        <Routes>
            <Route path="/" element={<Main/>}></Route>
            <Route path="/edit-profile"
               element={<EditProfile  authUser={authUser} setAuthUser={setAuthUser}
                        setEncodedCredentials={setEncodedCredentials}
                        encodedCredentials={encodedCredentials}
               />}>                
            </Route>
        </Routes>        
    )

}

export default Home; 
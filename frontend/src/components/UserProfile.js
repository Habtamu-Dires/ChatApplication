import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function UserProfile(props) {

    const [data, setData] = useState(null);
    const navigate = useNavigate();

    console.log("groupName " + props.groupName);

    const fechUserProfile = async () => {
        const response = await 
            fetch(`http://localhost:8080/api/v1/users/profile/${props.username}`,{
                headers: {
                    "Authorization": `Bearer ${props.authUser.jwtToken}`,
                    "Content-Type": "application/json",
                },               
                credentials: "same-origin"
            });
        const apiRes = await response.json();
        if(apiRes.success){
            setData(apiRes.data);
        }
    }

    useEffect(()=>{
        fechUserProfile();
    },[]);

    const addToContact = () => {
        fetch('http://localhost:8080/api/v1/users/add-to-contact',{
            method: "POST",
            body: JSON.stringify({username: props.authUser.username, 
                contactName: props.username
            }),
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){
                console.log(apiRes.message);
                props.setDisplay('SEARCH');
                props.fetchUserContacts();
                navigate('/');
            } else {
                alert(apiRes.message)
            }
        })
        .catch(err => console.log(err));
    }

    //add user to a group
    const addToGroup = () => {
        console.log("The grup name is " + props.groupName);
        console.log("The username is " + props.username);
        fetch('http://localhost:8080/api/v1/group-chat/add-to-group',{
            method: "POST",
            body: JSON.stringify({
                'groupName': props.groupName,
                'addedBy': props.authUser.username, 
                'username': props.username
            }),
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){
                console.log(apiRes.message);
                props.setDisplay('SEARCH');    
            }else{
                console.log(apiRes.message);
            }
        })
        .catch(err => console.log(err));
    }

    return(
        <div>
            {data !== null &&<div> <div>
                <p><span>UserName: </span>{data.username}</p>
                <p><span>Name: </span><span>{data.firstName}</span></p>
                <p><span>PhoneNumber: </span>{data.phoneNumber}</p>
            </div> 
            <div>
                {(props.username !== props.authUser.username) &&
                (props.groupName === 'null') &&
                <button type="button" onClick={addToContact}>Add</button>}
                {(props.username !== props.authUser.username) && 
                (props.groupName !== 'null') &&<div>
                <p>Add  user {props.username} to group {props.groupName}</p>
                <button type="button" onClick={addToGroup}>Add</button>
                </div>
                }
            </div>
            </div>            
            }            
        </div>
    );
}

export default UserProfile;
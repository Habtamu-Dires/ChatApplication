import React, { useState, useEffect } from 'react';
import debounce from 'lodash.debounce';
import UserProfile from './UserProfile';

const UserSearch = (props) => {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [display, setDisplay] = useState("SEARCH");
    const [selectedUser, setSelectedUser] = useState();    

    function encodeCredentials(username, password) {
        return btoa(`${username}:${password}`)
    }
    const encodedCredentials = encodeCredentials(
        props.authUser.username, props.authUser.password
    );

    const debouncedFetchUsers = debounce( (query) => {
        if (query) {
         fetch(`http://localhost:8080/api/v1/users/search?username=${query}`,{
                headers: {
                    "Authorization": `Basic ${encodedCredentials}`,
                    "Content-Type": "application/json",
                },
                credentials: "same-origin"
            })
            .then(response => response.json())
            .then(apiRes => {
                if(apiRes.success){
                    let dataList = [];
                    apiRes.data.forEach(element => {
                        if(element.username !== props.authUser.username){
                            dataList.push(element);
                        }
                    });
                    setSuggestions(apiRes.data);
                    console.log(apiRes.data);
                } else{
                    alert(apiRes.message);
                }
            })
            .catch(err => console.log(err));
            
        } else {
            setSuggestions([]);
        }
    }, 300);

    useEffect(() => {
        debouncedFetchUsers(query);
        return () => {
            debouncedFetchUsers.cancel();
        };
    }, [query]);

    const handleChange = (event) => {
        setQuery(event.target.value);
    };

    const handleUserClick = (username)=>{
        setSelectedUser(username);
        setDisplay("PROFILE")
    }

    return (
        <div>
            {display === "SEARCH" &&<div>
            <input
                type="text"
                value={query}
                onChange={handleChange}
                placeholder="Search for users..."
            />
            <ul>
                {suggestions.map((user) => (                   
                    <li key={user.username} style={{cursor:'pointer'}}
                        onClick={()=>handleUserClick(user.username)
                    }> 
                        {user.username}
                    </li>                                    
                ))}
            </ul>
          </div>}
            {display === "PROFILE" &&
                <UserProfile
                    setDisplay={setDisplay}
                    groupName={props.groupName}
                    fetchUserContacts={props.fetchUserContacts}
                    authUser={props.authUser}
                    username={selectedUser} 
                    encodedCredentials={encodedCredentials}
                />
            }
        </div>        
    );
};

export default UserSearch;
/**
 * Using lodash.debounce, you can delay the API call until the user stops typing for 
 * a specified time (e.g., 300 milliseconds).
 *  This reduces the number of API requests and improves performance.
 */
import React, {useState, useEffect, useRef} from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import UserSearch from './UserSearch';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

var stompClient = null;
const ChatRoom = (props) => {

    const navigate = useNavigate();
    
    const[file,setFile] = useState(null);
    const fileInputRef = useRef(null);
    const [showGroupPopUp, setShowGroupPopUp] = useState(false);
    const [groupOwner, setGroupOwner] = useState(null);
    const [groupName, setGroupName] = useState(null);
    const [groups, setGroups] = useState(new Map());
    const EMOJI = {"THUMBUP": '👍', "LOVE":'❤️', "CRYING":'😢', "SURPRISED":'😯'}

    const [privateChats, setPrivateChats] = useState(new Map());     
    const [contacts, setContacts] = useState(new Map());   
    const [tab,setTab] =useState("SEARCH");
    const [mode, setMode] = useState("MESSAGE");
    const [userData, setUserData] = useState({
        username: localStorage.getItem("username"),
        receivername: '',
        connected: false,
        message: ''
      }); 
      useEffect(() => {
        connect();
        // Clean up on unmount
        return () => {
            if (stompClient) {
                stompClient.deactivate();
            }
        };
    }, []);

    const headers = {
        "Authorization":`Bearer ${props.authUser.jwtToken}`
    };
    const connect = () => {
        const socket = new SockJS('http://localhost:8080/ws');
        stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: headers,
            debug: (str) => console.log(str),
            onConnect: onConnected,
            onStompError: onError,
        });

        stompClient.activate();
    };

    const onConnected = () => {
        setUserData((prevState) => ({ ...prevState, connected: true }));
        fetchUserContacts();
        fetchUserGroups();
        stompClient.subscribe(`/user/${userData.username}/my-messages`, onPrivateMessage);
        stompClient.subscribe(`/user/${userData.username}/my-groups`, onGroupMessage);
        stompClient.subscribe(`/user/${userData.username}/message-reaction`, onMssageReaction);
        console.log("User has joined ");
       
    };

    const onError = (error) => {
        console.log("Error connecting to WebSocket: ", error);
        setUserData((prevState) => ({ ...prevState, connected: false }));
    };
    
    //fetch contacts
    const fetchUserContacts = () => {
        fetch(`http://localhost:8080/api/v1/users/contacts/${userData.username}`,{
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => {
            if(response.ok){
               return response.json()
            } else if(response.status === 403){ // unauthorized
                localStorage.removeItem('username');
                localStorage.removeItem('jwtToken');
                props.setAuthUser(null);
            
            }
        })
        .then(apiRes => {
            console.log(apiRes);
            if(apiRes.success){
                apiRes.data.forEach(user => {
                    contacts.set(user.username,[]);
                    setContacts(new Map(contacts));
                });
                apiRes.data.forEach(user => {
                    privateChats.set(user.username, []);
                    setPrivateChats(new Map(privateChats));
                });
            }
        })
        .catch(err => console.log("error " + err));
    }

    //fech groups
    const fetchUserGroups =()=> {
        console.log("User group is bing called ");
        fetch(`http://localhost:8080/api/v1/users/groups/${props.authUser.username}`,{
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            groups.clear();
            setGroups(new Map(groups));
            if(apiRes.success){
                apiRes.data.forEach(groupName => {
                    groups.set(groupName,[]);
                    setGroups(new Map(groups));
                });
                apiRes.data.forEach(groupName => {
                    groups.set(groupName, []);
                    setGroups(new Map(groups));
                });
            }
        })
        .catch(err => console.log(err));
    }

    //on message reaction
    const onMssageReaction = (payload) => {
        let payloadData = JSON.parse(payload.body);
        if(payloadData.groupName !== null){
            fetchGroupMessages(payloadData.groupName);
        }
        else if(payloadData.sender === userData.username){
            fetchChatMessages(payloadData.recipient);
        } else {
            fetchChatMessages(payloadData.sender);
        }
        
    }

    // private messagsse
    const onPrivateMessage = (payload)=>{ 
        console.log(payload);
        let payloadData = JSON.parse(payload.body);
        if(payloadData.sender === userData.username){
            fetchChatMessages(payloadData.recipient);
        }
        else if(privateChats.get(payloadData.sender)){
            // privateChats.get(payloadData.sender).push(payloadData);
            // setPrivateChats(new Map(privateChats));
            fetchChatMessages(payloadData.sender);
        }else{ 
            let list =[];
            list.push(payloadData);
            privateChats.set(payloadData.sender,list);
            setPrivateChats(new Map(privateChats));
            contacts.set(payloadData.sender,[]);
            setContacts(new Map(contacts));

        }
    }

    //onGroupMessage
    const onGroupMessage = (payload) => {
        let payloadData = JSON.parse(payload.body);
        console.log("The recipient: " + payloadData.recipient);
           
        if(payloadData.recipient && payloadData.recipient === 'DELETE') {
            if(groups.get(payloadData.groupName)){
                groups.delete(payloadData.groupName);
                setGroups(new Map(groups));
            }
            //fetchUserGroups();
            setTab("SEARCH");
        } 
        else if(groups.get(payloadData.groupName)){
            // groups.get(payloadData.groupName).push(payloadData);
            // setGroups(new Map(groups));
            fetchGroupMessages(payloadData.groupName);
        }else{ 
            let list =[];
            list.push(payloadData);
            groups.set(payloadData.groupName,list);
            setGroups(new Map(groups));
        }
    }
    //send attachment
    const sendAttachment = async () => {
        const formData = new FormData();
       formData.append('file', file);
      const response = 
            await  fetch('http://localhost:8080/api/v1/attachments/upload-file', {
                            method: "POST",
                            body: formData,
                            headers: {
                                "Authorization": `Bearer ${props.authUser.jwtToken}`
                            },
                            credentials: "same-origin"
                    });
        const apiRes = await response.json();
        console.log(apiRes);

        if(apiRes.success){
            console.log(apiRes.data);
            return apiRes.data;
        } 
        return null;
    }

    // handle message value on change
    const handleTextChange =(event)=>{
        const {value}=event.target;
        setUserData({...userData,"message": value});
    }

    //send private message
    const sendPrivateMessage= async ()=>{
        let fileDTO = null;
        let send = true;
        if(file !== null){
            send = false;
             fileDTO = await sendAttachment(file);
            if(fileDTO !== null){
                send = true;
            }
            
         }

        if(send) {
            if(fileDTO === null){
                fileDTO = {fileName: '', fileUrl: ''}
            }

        var chatMessage = {
            sender: userData.username,
            recipient:tab,
            text: userData.message,
            fileName: fileDTO.fileName,
            fileUrl: fileDTO.fileUrl,
            type: "private",
        };
            
            // if(userData.username !== tab){
            //     privateChats.get(tab).push(chatMessage);
            //     setPrivateChats(new Map(privateChats));
            // }
            
            const response = await axios.post('http://localhost:8080/api/v1/chat/send',
                JSON.stringify(chatMessage),{
                headers: {
                    "Authorization": `Bearer ${props.authUser.jwtToken}`,
                    "Content-Type": "application/json",
                },
                credentials: "same-origin"
            });
            const apiRes = await response.data;
            console.log("The api RES " + apiRes)
            if(apiRes.success){
                console.log(apiRes.message);
                setUserData({...userData,"message": ""});
                setFile(null);
                fileInputRef.current.value ='';
                
            } else {
                alert(apiRes.message);
            }
        
        }
    }

    //send group message
    const sendGroupMessage = async () =>{
        let fileDTO = null;
        let send = true;
        if(file !== null){
            send = false;
             fileDTO = await sendAttachment(file);
            if(fileDTO !== ''){
                send = true;
            }
            console.log("The filePaht " + fileDTO.fileUrl)
         }

        if(send) {
            if(fileDTO === null){
                fileDTO = {fileName: '', fileUrl: ''}
            }
            var chatMessage = {
                sender: userData.username,
                groupName:tab,
                text: userData.message,
                fileName: fileDTO.fileName,
                fileUrl: fileDTO.fileUrl,
                type: "group",
            };
                
            
            fetch(`http://localhost:8080/api/v1/group-chat/send`,{
                method: "POST",
                body: JSON.stringify(chatMessage),
                headers: {
                    "Authorization": `Bearer ${props.authUser.jwtToken}`,
                    "Content-Type": "application/json",
                },
                credentials: "same-origin"
            })
            .then(response => response.json())
            .then(apiRes =>{
                if(apiRes.success){
                    console.log(apiRes.message);
                    setUserData({...userData,"message": ""});
                    setFile(null);
                    fileInputRef.current.value='';
                } else {
                    alert(apiRes.message);
                }
            })
            .catch(err => console.log(err));
        }
        
    }

    const handleTablClick = (username) => {
        setTab(username);
        setMode("MESSAGE");
        fetchChatMessages(username);    
    }

    const handleGroupTablClick=(groupName) => {
        setTab(groupName);
        setMode("GROUP");
        fetchGroupMessages(groupName);
    }
    // fetch private messages
   async function fetchChatMessages(username){
        
       const response = await axios.get(`http://localhost:8080/api/v1/chat/messages/${userData.username}/${username}`,{
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        });
        const apiRes = response.data;

        if(apiRes.success){
            if(!privateChats.get(username)){
                privateChats.set(username, []);
                setPrivateChats(new Map(privateChats));
            } else{
                privateChats.delete(username);
                privateChats.set(username, []);
                setPrivateChats(new Map(privateChats));
            }
            if(apiRes.data.length !== 0){
                apiRes.data.forEach(chatNotif => {
                    chatNotif.showReactionOPtions = false;
                    privateChats.get(username)
                        .push(chatNotif);
                    setPrivateChats(new Map(privateChats));
                });
            }
        }    
    }

    // fetch a group messages
    function fetchGroupMessages(groupName)  {
        fetch(`http://localhost:8080/api/v1/group-chat/messages/${groupName}`,{
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){

                if(!groups.get(groupName)){
                    groups.set(groupName, []);
                    setGroups(new Map(groups));
                } else{
                    groups.delete(groupName);
                    groups.set(groupName, []);
                    setGroups(new Map(groups));
                }
                let groupOwner;
                if(apiRes.data.length !== 0) {
                    apiRes.data.forEach(chatNotif => {
                        groupOwner = chatNotif.groupOwner;
                        chatNotif.showReactionOPtions = false;
                        groups.get(groupName)
                            .push(chatNotif);
                        setGroups(new Map(groups)); 
                    });
                    setGroupOwner(groupOwner);
                }
            } else{
                console.log("you got error ? ")
                console.log(apiRes.message)
            }
        })
        .catch(err => console.log(err));
    }

    const updateProfile = () => {
        navigate('/edit-profile')
    }

    //create group 
    const handleGroupNameChange = (event) => {
        setGroupName(event.target.value);
    }

    const createGroup = (event) => {
        //popup to get groupname
        event.preventDefault();
        const createGroup = {
            'ownerName': props.authUser.username,
            'groupName': groupName
        }
        fetch('http://localhost:8080/api/v1/group-chat/create-group',{
            method: 'POST',
            body:JSON.stringify(createGroup),
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes =>{
            if(apiRes.success){
                groups.set(groupName, []);
                setGroups(new Map(groups))
                // fetchUserGroups();
            }
        })
        .catch(err => console.log(err));

        setShowGroupPopUp(false);
    } 
    // add user to a group
    const addUserToGroup = () => {
        setMode("ADDTOGROUP");
    }
    // delete a group.
    const deleteGroup = () =>{
        fetch(`http://localhost:8080/api/v1/group-chat/delete-group/${tab}/${userData.username}`,{
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => response.json())
        .then(apiRes => {
            if(apiRes.success){
                groups.delete(tab);
                setGroups(new Map(groups));
                setMode("MESSAGE");
                setTab("SEARCH");           

                console.log("wow");
            } else {
                console.log(apiRes.message)
            }
        })
        .catch(err => console.log(err));
    };

    // attachment 
    const handleFileChange = (e) => {
        console.log("hello ")
        const selectedFile = e.target.files[0];
        if (selectedFile) {
          setFile(selectedFile); // Note: file.path might not work in web browsers due to security reasons. Use file.name instead.
        }
      };
    
      const handleAttachmentClick = () => {
        if(fileInputRef.current){
            fileInputRef.current.click();
        }
      }

      const handleFileRemove = () => {
        setFile(null);
        if(fileInputRef.current){
            fileInputRef.current.value='';
        }
      };

      //reaction click
      const handleReactionClick = async (messageId, emoji) => {
        console.log("id " + messageId + " and " + emoji);
        const message = {
            "chatMessageId": messageId,
            "username": userData.username,
            "emoji": emoji
        }
       const response = await axios.post('http://localhost:8080/api/v1/chat-reaction/add-remove'
        ,JSON.stringify(message),{
        
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin" 
        })
        const apiRes = await response.data;
        if(apiRes.success){
          // await fetchChatMessages(tab);
        } else {
            alert(apiRes.message);
        }        
      }

    //logout
    const logout = () => {
        
        fetch('http://localhost:8080/api/v1/logout', {
            method: 'POST',
            headers: {
                "Authorization": `Bearer ${props.authUser.jwtToken}`,
                "Content-Type": "application/json",
            },
            credentials: "same-origin"
        })
        .then(response => {
                props.setAuthUser(null);
                localStorage.removeItem("jwtToken");
                localStorage.removeItem("username");
        })
        .catch(err => {
            console.log(err);
            props.setAuthUser(null);
            localStorage.removeItem("jwtToken");
            localStorage.removeItem("username");
        });
    }
    // showReactions
    const showChatRectionForPrivatteMesg = (index, show) => {
        
        const chatArray = privateChats.get(tab);
        const obj = chatArray[index];
        obj.showReactionOPtions = show;
        console.log(obj);
        chatArray[index] = obj;
    
        privateChats.set(tab, chatArray);
        setPrivateChats(new Map(privateChats));

      };
      const showChatRectionForGroupMesg = (index, show) => {
        
        const chatArray = groups.get(tab);
        const obj = chatArray[index];
        obj.showReactionOPtions = show;
        console.log(obj);
        chatArray[index] = obj;
    
        groups.set(tab, chatArray);
        setGroups(new Map(groups));

      };
    
      const showReactionOptions = (index, type) => {
        if(type === 'private'){
            showChatRectionForPrivatteMesg(index, true);
        } else if(type === 'group'){
            showChatRectionForGroupMesg(index, true)
        }
      };
    
      const hideChatReactionOptions = (index, type) => {
        if(type === 'private'){
            showChatRectionForPrivatteMesg(index, false);
        } else if(type === 'group'){
            showChatRectionForGroupMesg(index, false)
        }
      };
    return (
    <div className="container">
        {userData.connected?
        <div className="chat-box">
            <div className="member-list">
                <div style={{height:'83%'}}>
                    <a onClick={updateProfile} style={{cursor:'pointer'}}>
                        {userData.username}
                    </a>
                    <ul>
                        <li onClick={()=>{setTab("SEARCH")}} className={`member ${tab==="SEARCH" && "active"}`}>
                            Search
                            </li>
                        {[...contacts.keys()].map((name,index)=>(
                            <li onClick={()=>handleTablClick(name)} className={`member ${tab===name && "active"}`} key={index}>
                                {name}
                            </li>
                            
                        ))}
                        {[...groups.keys()].map((groupName,index)=>(
                            <li onClick={()=>handleGroupTablClick(groupName)} className={`member ${tab===groupName && "active"}`} key={index}>
                                {groupName}
                            </li>
                            
                        ))}
                        
                    </ul>
                </div>                
                <div>
                    <a onClick={()=>setShowGroupPopUp(true)} style={{cursor:'pointer'}}>Create Group</a>
                    {showGroupPopUp && (
                        <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'white', padding: '20px' }}>
                        <h3>Enter a Group Name 
                        <a style={{color:'red' ,cursor:'pointer'}} onClick={() => setShowGroupPopUp(false)}> X</a>
                        </h3>
                        <form>
                          <input type="text" value={groupName} onChange={handleGroupNameChange} />
                          <a style={{cursor:'pointer'}} onClick={createGroup} >Create</a>
                        </form>
                        
                      </div>
                    )}

                </div>
                <div>
                    <a onClick={logout} style={{cursor: 'pointer'}}>logout</a>
                </div>
            </div>
            
            {(tab==="SEARCH") && <div className='chat-content'>
                <UserSearch  groupName={'null'} authUser={props.authUser} fetchUserContacts={fetchUserContacts}                /> 
            </div>}
            {(tab!=="SEARCH" && mode==="MESSAGE") && 
            <div className='chat-content'>
                <ul className="chat-messages">
                    {[...privateChats.get(tab)].map((chat,index)=>(<>
                        <li className={`message ${chat.sender === userData.username && "self"}`} key={index}>
                            {chat.sender !== userData.username && <div className="avatar">{chat.sender}</div>}
                            <div className="message-data">{chat.text}</div>
                            <div>{Object.keys(chat.reactions).length !== 0}</div>                                       

                            {chat.fileUrl !== '' && 
                            <div>
                                 <a href={chat.fileUrl} download={chat.fileName} style={{ color: 'blue', textDecoration: 'underline' }}>
                                {chat.fileName}
                                </a>                                    
                            </div>
                            }

                            {chat.sender === userData.username && <div className="avatar self">{chat.sender}</div>}
                            
                        </li>
                        <div onMouseEnter={()=>{showReactionOptions(index, 'private')}} 
                             className={`message  ${chat.sender === userData.username && "self"}`}>
                            {Object.keys(chat.reactions).length !== 0 && (
                                    <div>
                                    {Object.keys(chat.reactions).map(key => (
                                        <span key={key}>{key} {EMOJI[chat.reactions[key]]}</span>
                                    ))}
                                    </div>)
                            } 
                        </div>
                        {chat.showReactionOPtions && 
                        <div onMouseLeave={() =>{hideChatReactionOptions(index, 'private')}}
                                 className={`message  ${chat.sender === userData.username && "self"}`}>    
                            <span onClick={()=> handleReactionClick(chat.id,'LOVE')} style={{cursor: 'pointer'}}>
                                    ❤️ </span>
                            <span onClick={()=> handleReactionClick(chat.id,'THUMBUP')} style={{cursor: 'pointer'}}>
                                👍 </span>
                            <span onClick={()=> handleReactionClick(chat.id,'CRYING')} style={{cursor: 'pointer'}}>
                                😢 </span>
                            <span onClick={()=> handleReactionClick(chat.id,'SURPRISED')} style={{cursor: 'pointer'}}>
                                😯 </span>
                        </div>}
                        </>
                    ))}
                </ul>

                <div className="send-message">
                    <div>
                        <input
                            type="text"
                            value={userData.message}
                            style={{width: '320px'}}
                            onChange={handleTextChange}
                            placeholder="Type a message"
                        />
                        <button onClick={handleAttachmentClick} 
                            style={{ cursor: 'pointer' }}>
                            📎
                        </button>
                        <input
                            type="file"
                            ref={fileInputRef}
                            style={{ display: 'none' }}
                            onChange={handleFileChange}
                        />
                        {file && (
                            <div>
                                <strong>Selected file:</strong> {file.name} ({(file.size / 1024).toFixed(2)} KB)
                                <button onClick={handleFileRemove} style={{ cursor: 'pointer', marginLeft: '10px' }}>
                                    ❌
                                </button>
                            </div>
                        )}
                    </div>
                    <button type="button" className="send-button" onClick={sendPrivateMessage}>send</button>
                </div>
                
            </div>}
            {(tab!=="SEARCH" && mode==="GROUP")&& <div className='chat-content'>
                <div>            
                    <span>
                        <a onClick={addUserToGroup} style={{cursor:'pointer', marginRight:'10px'}}>
                            Add User
                        </a>
                        {groups.get(tab).length == 0  && (
                            <a onClick={deleteGroup} style={{cursor:'pointer',float:'right'}}>
                            Delete
                            </a>    
                        )}
                        {groups.get(tab).length !== 0 && groupOwner === userData.username && (
                             <a onClick={deleteGroup} style={{cursor:'pointer',float:'right'}}>
                             Delete
                             </a> 
                        )}
                        
                    </span>
                </div>
                <ul className="chat-messages">
                    {[...groups.get(tab)].map((chat,index)=>(<>
                        {chat.type ==='group' && <>
                            <li className={`message ${chat.sender === userData.username && "self"}`} key={index}>
                            {chat.sender !== userData.username && <div className="avatar">{chat.sender}</div>}
                            
                            <div className="message-data">{chat.text}</div>                            

                            {chat.fileUrl !== '' && 
                            <div>
                                 <a href={chat.fileUrl} download={chat.fileName} style={{ color: 'blue', textDecoration: 'underline' }}>
                                {chat.fileName}
                                </a>                                    
                            </div>
                            }
                            
                            {chat.sender === userData.username && <div className="avatar self">{chat.sender}</div>}
                        
                        </li>
                        <div onMouseEnter={()=>{showReactionOptions(index, 'group')}} 
                            className={`message  ${chat.sender === userData.username && "self"}`}>
                            {Object.keys(chat.reactions).length !== 0 && (
                                    <div className='self'>
                                    {Object.keys(chat.reactions).map(key => (
                                        <span key={key}>{key} {EMOJI[chat.reactions[key]]}</span>
                                    ))}
                                    </div>)
                            } 

                        </div>
                        {chat.showReactionOPtions && 
                            <div onMouseLeave={()=>{hideChatReactionOptions(index, 'group')}}
                                 className={`message  ${chat.sender === userData.username && "self"}`}>    
                            <span onClick={()=> handleReactionClick(chat.id,'LOVE')} style={{cursor: 'pointer'}}>
                                    ❤️ </span>
                            <span onClick={()=> handleReactionClick(chat.id,'THUMBUP')} style={{cursor: 'pointer'}}>
                                👍 </span>
                            <span onClick={()=> handleReactionClick(chat.id,'CRYING')} style={{cursor: 'pointer'}}>
                                😢 </span>
                            <span onClick={()=> handleReactionClick(chat.id,'SURPRISED')} style={{cursor: 'pointer'}}>
                                😯 </span>
                        </div>}
                        </>
                        }
                        {chat.type === 'USER-ADDED' && 
                            <div className='usedAddedMsg'>{chat.text}</div>
                        }
                        
                        </>
                    ))}
                </ul>

                <div className="send-message">
                <div>
                    <input
                        type="text"
                        value={userData.message}
                        style={{width: '320px'}}
                        onChange={handleTextChange}
                        placeholder="Type a message"
                    />
                    <button onClick={handleAttachmentClick} 
                        style={{ cursor: 'pointer' }}>
                        📎
                    </button>
                    <input
                        type="file"
                        ref={fileInputRef}
                        style={{ display: 'none' }}
                        onChange={handleFileChange}
                    />
                    {file && (
                        <div>
                        <strong>Selected file:</strong> {file.name} ({(file.size / 1024).toFixed(2)} KB)
                        <button onClick={handleFileRemove} style={{ cursor: 'pointer', marginLeft: '10px' }}>
                            ❌
                        </button>
                        </div>
                    )}
                </div>
                    <button type="button" className="send-button" onClick={sendGroupMessage}>send</button>
                </div>
                
            </div>}
            {(tab !== "SEARCH") && (mode==="ADDTOGROUP") && 
                <UserSearch  groupName={tab} authUser={props.authUser} fetchUserContacts={fetchUserContacts}/> 
            }            
        </div>
        :
        <div className="register">
            <p>Loading .... </p>
            <p>Please wait </p>
        </div>}
    </div>
    )
}

export default ChatRoom

<!DOCTYPE html>

<html>
    <head>
        <meta charset="UTF-8">
        <title>Cruise Chat</title>
        <link href="https://fonts.googleapis.com/css?family=Asap" rel="stylesheet">
        <link rel="stylesheet" href="css/styles.css">
    </head>

    <body>
        <section id="appIntro">
            <div id="titleSection">
                <img src="img/CruiseAgent.gif" height="460" width="240" class="center">
                <h1 id="appTitle">Cruise Agent AI</h1>
                <div class="line"></div>
                <div class="headerImage"></div>
            </div>
            <div class="msSection" id="messages">
                <div class="headerRow">
                    <div class="headerIcon">
                        <img src="img/sysProps.svg"/>
                    </div>
                    <div class="headerTitle" id="sysPropTitle">
                        <h2>Cruise Agent AI for Gold Loyalty Tier</h2>
                    </div>
                </div>
                <div class="sectionContent">
                    <table id="chatTable">
                        <tbody id="messagesTableBody">
                        <tr>
                            <th width="60%">Chat Messages</th>
                            <th width="10%">Time</th>
                            <th></th>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <div class="my_message_container">
            <label id="myMsgLabel">My message:</label>
            <input type="text" size="50" id="myMessage"/>
            <button id="sendButton" type="button" onclick="sendMessage()">Send</button>
        </div>

        <footer class="bodyFooter">
            <div class="bodyFooterLink">
                <div id="footer_project_container">
                    <span id="footer_cruise_chat></span>
                    <p id="footer_project">A Savoir Technologies Demo</p>
                </div>
                <img src="img/footer.png"></img>
            </div>
            <p id="footer_copyright">&copy;Copyright Savoir Technologies 2024</p>
        </footer>

        <!-- JavaScript -->
        <script src="chatroom.js"></script>

    </body>
</html>

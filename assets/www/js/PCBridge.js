/*
 *  PCBridge v.2
 *  
 *  Share content from Android to PC
 *  
 *  Developed by o3r3
 *  Carlos Villanueva
 *
 *  o3r3.tinc@gmail.com
 *  
 */

/*
 * Protocol
 */

var prot = "ws://",

/*
 * IP
 */


ip = "",

/*
 * Port
 */


port = ":8887/",

/*
 * URI
 */


wsUri = prot + ip + port,

/*
 * ListView to show content of shared
 */


lista,


/*
 * state of connection
 */


conexion,


websocket,

/*
 * definimos colores conectado, desconectado
 */

colorConectado = '#00bdfe',
colorDesconectado = '#F08080',

divButton;

//url;


/*
 * Starting
 *
 */

function init() {
    try {
        ip = window.location.hostname;
        $('#status1').css("color", colorDesconectado);
        divButton = $('#secondButton');
        divButton.css("display", "none");
        lista = $('#listaCompartidos').listview();
        lista.on('click', 'a', function (e) {
            var urlPicture = $(this).text();
            if ($(this).get(0).id == "ima") {
                downloadImage(urlPicture);
            }
        });
        $('#status1').css("color", colorDesconectado);
        testWebSocket();
        handleReceiveButtons('disable');
        handleConnectButton('enable');
        handleDeleteSelectedButton('disable');
        startConnection();
        localizame();      
    } catch (e) {
        location.reload(true);
    }
}   


function localizame() {
    $('#titulo').text(localize("%titulo"));
    $('#status1').text(localize("%status1"));
    $('#botonConectar').text(localize("%botonConectar"));
    $('#botonConectar').button("refresh");
    $('#botonRecibirNuevos').text(localize("%botonRecibirNuevos"));
    $('#botonRecibirNuevos').button("refresh");
    $('#botonRecibirViejos').text(localize("%botonRecibirViejos"));
    $('#botonRecibirViejos').button("refresh");
    $('#botonBorrarSeleccionados').text(localize("%botonBorrarSeleccionados"));
    $('#botonBorrarSeleccionados').button("refresh");
    $('#botonBorrarViejos').text(localize("%botonBorrarViejos"));
    $('#botonBorrarViejos').button("refresh");
    $('#botonConectar1').text(localize("%botonConectar"));
    $('#botonConectar1').button("refresh");
    $('#botonRecibirNuevos1').text(localize("%botonRecibirNuevos"));
    $('#botonRecibirNuevos1').button("refresh");
    $('#botonRecibirViejos1').text(localize("%botonRecibirViejos"));
    $('#botonRecibirViejos1').button("refresh");
    $('#botonBorrarSeleccionados1').text(localize("%botonBorrarSeleccionados"));
    $('#botonBorrarSeleccionados1').button("refresh");
    $('#botonBorrarViejos1').text(localize("%botonBorrarViejos"));
    $('#botonBorrarViejos1').button("refresh");
    $('#clearHist').text(localize("%clearHist"));
    $('#yes').text(localize("%yes"));
    $('#yes').button("refresh");
    $('#yes1').text(localize("%yes1"));
    $('#yes1').button("refresh");
    $('#no').text(localize("%no"));
    $('#no').button("refresh");
    $('#delSel').text(localize("%delSel"));
    $('#undone').text(localize("%undone"));
    $('#undone1').text(localize("%undone1"));
}


function takeCareCheckBox() {
    $("input[type='checkbox']").change(
            function (){
                var checked = [];
                $(":checkbox").each(
                    function() {
                        if(this.checked){
                            checked.push(this.id);
                        } 
                        if (checked.length != 0) {
                            handleDeleteSelectedButton('enable');
                        } else {
                            handleDeleteSelectedButton('disable');
                        }
                    }
            );
        });
}

function handleReceiveButtons(b) {
    $('[id^=botonRecibirNuevos]').button(b);
    $('[id^=botonRecibirViejos]').button(b);
    $('[id^=botonBorrarViejos]').button(b);
}

function handleDeleteSelectedButton(b) {
    $('[id^=botonBorrarSeleccionados]').button(b);
}

function handleConnectButton(b) {
    $('[id^=botonConectar]').button(b);
}

 /*
  *Alert user if is using wrong web browser
  */

function testWebSocket () {
    if (!window.WebSocket) {
       alert("WebSocket not supported :( Use an updated FireFox, Chrome or Safari");
    }
}

/*
 *Changing background color depending of estado
 *
 *true ok, false nok red
 */
function alertInput(estado) {
    if (estado) {
        $('#status1').css("color",'white');
        $('.inputColor').parent().css("background-color",'white');
    } else {
        $('#status1').css("color",colorDesconectado);
        $('.inputColor').parent().css("background-color",colorDesconectado);
    }
}
    
/*
 * start webSocket
 */
    
function openWebSocket() {
    wsUri = prot + ip + port;
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
        onOpen(evt)
        };
    websocket.onclose = function(evt) {
        onClose(evt)
        };
    websocket.onmessage = function(evt) {
        onMessage(evt)
        };
    websocket.onerror = function(evt) {
        onError(evt)
        };
}
        
function onOpen(evt) {
    conexion = true;
    $('#status1').text(localize("%status2"));
    $('#status1').css("color", colorConectado);
    handleReceiveButtons('enable');
    handleConnectButton('disable');
    sendMeData();
}
        
function onClose(evt) {
    conexion = false;
    $('#status1').text(localize("%status1"));
    $('#status1').css("color", colorDesconectado);
    handleReceiveButtons('disable');
    handleDeleteSelectedButton('disable');
    handleConnectButton('enable');
    if (websocket) {
        websocket.close();
        websocket = null;
    }
}
        
function onMessage(evt) {
    writeToScreen( evt.data);
    takeCareCheckBox();
}
        
function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}
        
function doSend(message) {
    try {
        websocket.send(message);
    } catch (e){
        
    }
}        
     
     
function writeToScreen(message) {
    lista.empty();
    lista.append();
    lista.listview('refresh');
    /*
     * take text from Android device and try parsing to json
     */
    if (message) {
        try {
            
        var obj = JSON && JSON.parse(message) || $.parseJSON(message);
            if (obj.listado.length != 0) {
                obj.listado.forEach(function(compartido){
                var arrayTexto = compartido.contenido.split(" ");
                var externalRef = false;
                for (var i in arrayTexto) {          
                    if (arrayTexto[i].indexOf("http://") != -1 || arrayTexto[i].indexOf("https://") != -1) {
                        var url;
                        if (arrayTexto[i].indexOf("http", 1) !=-1) {
                            url = arrayTexto[i].substring(arrayTexto[i].indexOf("http"), arrayTexto[i].length);
                        } else {
                            url = arrayTexto[i];
                        }
                        lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" target="_blank" href="'+url+'"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        externalRef = true;
                        divButton.show();
                    } else if (arrayTexto[i].indexOf(".jpg") != -1 ||
                        arrayTexto[i].indexOf(".JPG") != -1 ||       
                        arrayTexto[i].indexOf(".png") != -1 ||
                        arrayTexto[i].indexOf(".PNG") != -1 &&
                        arrayTexto[i].indexOf("http") == -1) {
                        url = compartido.contenido;
                        //lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" target="_blank" href="stuff/PCBridge_pictures.html"><image class="thumbnail" src="' + url + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        //lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" onClick="downloadImage(' + url +')"><image class="thumbnail" src="' + url + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" id="ima"><image class="thumbnail" src="' + url + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        externalRef = true;
                        divButton.show();
                    } else if (arrayTexto[i].indexOf(".3gp") != -1 ||
                               arrayTexto[i].indexOf(".3GP") != -1) {
                        url = compartido.contenido;
                        lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" id="ima"><image class="thumbnail" src="' + window.location.origin + '/css/images/ic_action_video.png' + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        externalRef = true;
                        divButton.show();
                    } else if (arrayTexto[i].indexOf(".aac") != -1 ||
                               arrayTexto[i].indexOf(".AAC") != -1) {
                        url = compartido.contenido;
                        lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" id="ima"><image class="thumbnail" src="' + window.location.origin + '/css/images/ic_action_volume_on.png' + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        externalRef = true;
                        divButton.show();
                    } else if (arrayTexto[i].indexOf(".amr") != -1 ||
                               arrayTexto[i].indexOf(".AMR") != -1) {
                        url = compartido.contenido;
                        lista.append('<li><input class="checkboxList" type="checkbox" id="' + compartido.id + '"> <a class="contenido" id="ima"><image class="thumbnail" src="' + window.location.origin + '/css/images/ic_action_volume_on.png' + '"><font style="white-space:wrap; font-size: small" >' + compartido.contenido.replace(/\n/g,'<br/>') + '</font></a></li>');
                        externalRef = true;
                        divButton.show();
                    }
                }
                if (!externalRef) {
                    lista.append('<li data-icon="false"><input class="checkboxText" type="checkbox" id="' + compartido.id + '">' + compartido.contenido.replace(/\n/g,'<br/>') + '</li>');
                      divButton.show();
                }
            })
            } else {
                lista.append('<li data-icon="false" style="text-align: center; letter-spacing:10px;">' + 'NO CONTENT :(' + '</li>');
                divButton.hide();
            }    
        } catch (e) {
            return;
        }
    } else {
        console.log('no hay mensaje :(');
        divButton.hide();
    } 
    lista.listview('refresh');
    receivedData();
    takeCareCheckBox();
}
        
/*
* try to open connection with android
* via new ip 
*/
        
function startConnection() {
    if (isEmpty(ip)) {
        alertInput(false);
    } else {
        alertInput(true);
        openWebSocket();
        sendMeData();
    }
}


function isEmpty(str) {
    return (!str || 0 === str.length);
}
        

function receivedData() {
    doSend('OK RECEIVED');
}

/*
* send something to recieve 
*/

function sendMeData() {
    doSend("OK OPEN");
}

/*
* send me pending shared
*/

function sendMeNews() {
    if (websocket) {
        websocket.send("OK OPEN");
    } 
    else {
        websocket = null;
        openWebSocket();
    }
}

/*
* send history shared
*/

function sendMeHist() {
    if (websocket) {
        websocket.send("HIST");
    } 
    else {
        websocket = null;
        openWebSocket();
    }
}
        
/*
* delete history shared
*/

function deleteHist() {
    if (websocket) {
        websocket.send("HIST_DEL");
    } 
    else {
        websocket = null;
        openWebSocket();
    }
    sendMeHist();
}

/*
* delete history selected
*/

function deleteHistSelected(selected) {
    if (websocket) {
        websocket.send("HIST_DEL_SEL:" + selected);
    } 
    else {
        websocket = null;
        openWebSocket();
    }
    handleDeleteSelectedButton('disable');
    sendMeHist();
}

function noClearHistory() {
    $('#popupBasic').popup('close');
}

function yesClearHistory() {
    try {
        deleteHist();
    } catch (e) {

    } finally {
        $('#popupBasic').popup('close');
    }
    
}

function deleteHistBtt() {
    $('#popupBasic').popup('open');
}

function deleteHistSelectedBtt() {
    $('#popupDeleteSelected').popup('open');
}



function deleteSelectedFromHistBtt() {
    var notChecked = [], checked = [];
        $(":checkbox").each(function() {
            if(this.checked){
                checked.push(this.id);
            } else {
                notChecked.push(this.id);
}
        });
      deleteHistSelected(checked.toString());
}

function yesClearSelected() {
    try {
        deleteSelectedFromHistBtt();
    } catch (e) {
        
    } finally {
        $('#popupDeleteSelected').popup('close');
    }
}

function noClearSelected() {
    $('#popupDeleteSelected').popup('close');
}

function downloadImage(url) {
            if (navigator.userAgent.indexOf('Firefox') != -1 && parseFloat(navigator.userAgent.substring(navigator.userAgent.indexOf('Firefox') + 8)) >= 3.6){//Firefox
                window.open(window.location.origin + url,'_blank','resizable=yes,top=-245,width=250,height=250,scrollbars=no');
            }else if (navigator.userAgent.indexOf('Chrome') != -1 && parseFloat(navigator.userAgent.substring(navigator.userAgent.indexOf('Chrome') + 7).split(' ')[0]) >= 15){//Chrome
                window.open(window.location.origin + url,'_blank');
                //esto.close();
                window.close();
            }else if(navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Version') != -1 && parseFloat(navigator.userAgent.substring(navigator.userAgent.indexOf('Version') + 8).split(' ')[0]) >= 5){//Safari
                window.location.reload();
            }
}

var localize = function (string, fallback) {
	var localized = string.toLocaleString();
	if (localized !== string) {
		return localized;
	} else {
		return fallback;
	}
};
        
window.addEventListener("load", init, false);

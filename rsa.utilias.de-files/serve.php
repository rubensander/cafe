<?php
if(isset($_POST['ip'])) {
    $ip = $_POST['ip'];
} else {
    $ip = $_SERVER['REMOTE_ADDR'];
    if(strpos($ip, ':') !== false)
        $ip = "[" . $ip . "]";
}

$ip = $ip . ":2853";

file_put_contents("cur_server.txt", $ip);
echo("Umleitung auf $ip wurde eingerichtet");
?>
<br><br>
<form action="/serve.php" method="post">
    <label for="ip">Manuelle IPv4:</label>
    <input type="text" name="ip">:2853
    <input type="submit" value="Festlegen">
</form>

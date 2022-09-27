<?php
// cafe.php
if(file_exists("cur_server.txt")) {
  $ip = file_get_contents("cur_server.txt");
  header("Location: http://".$ip, TRUE, 302);
} else {
  echo("Es scheint kein Cafe-Server zu laufen");
}
?>

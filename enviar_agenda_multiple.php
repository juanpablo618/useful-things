<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require 'vendor/autoload.php';

date_default_timezone_set('America/Argentina/Buenos_Aires');

// Conexión a la base de datos
$servername = "10.100.23.234";
$username = "root";
$password = "BIOeqi72215";
$dbname = "estudioAlvarez";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    file_put_contents("log.txt", "Conexión fallida: " . $conn->connect_error . PHP_EOL, FILE_APPEND);
    die("Conexión fallida: " . $conn->connect_error);
}

$today = date("Y-m-d");

// Mapeo de responsables a emails
$responsables = [
//    "Paula Alvarez" => "pau.alvarez@estudioalvarezyasociados.com.ar",
  //  "Camila A Ruiz Diaz" => "camila.ruizdiaz@estudioalvarezyasociados.com.ar",
    //"Liliana Romero" => "liliana.romero@estudioalvarezyasociados.com.ar",
    //"Ayelen Brizzio" => "ayelen.brizzio@estudioalvarezyasociados.com.ar",
    "Mateo Francisco Alvarez" => "mateo.alvarez@estudioalvarezyasociados.com.ar",
    //"María Emilia Campos" => "emilia.campos@estudioalvarezyasociados.com.ar",
    //"Mateo Novau" => "mateo.novau@estudioalvarezyasociados.com.ar",
    //"Natali D Agostino" => "nati.dagostino@estudioalvarezyasociados.com.ar",
    //"María Jose Alaye" => "jose.alaye@estudioalvarezyasociados.com.ar",
    //"Ezequiel Brener" => "ezequiel.brener@estudioalvarezyasociados.com.ar",
    //"Catalina Povarchik" => "catalina.povarchik@estudioalvarezyasociados.com.ar",
    //"Valentina Fourcade Jerez" => "valentina.fourcade@estudioalvarezyasociados.com.ar",
    //"Paola Maldonado" => "paola.maldonado@estudioalvarezyasociados.com.ar",
    //"Pilar Boglione" => "pili.boglione@estudioalvarezyasociados.com.ar",
    //"Amparo Alanis Toledo" => "ampi.alanis@estudioalvarezyasociados.com.ar"
];

foreach ($responsables as $responsable => $email) {
    $sql = "SELECT * FROM Agenda WHERE fecha = '$today' AND responsable = '$responsable'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        $body = "Agendas del dia para $responsable:\n\n";
        while($row = $result->fetch_assoc()) {
            $fechaFormateada = (new DateTime($row["fecha"]))->format('d-m-Y');
            $body .= "Fecha: " . $fechaFormateada . ", Descripcion: " . $row["descripcion"] . ", Realizado: " . $row["realizado"] . "\n";
        }

        $mail = new PHPMailer(true);
        try {
            $mail->isSMTP();
            $mail->Host = 'smtp-relay.brevo.com';
            $mail->SMTPAuth = true;
            $mail->Username = '8c5be0001@smtp-brevo.com';
            $mail->Password = 'gbOKBHdZn1zAE6Ja';
            $mail->SMTPSecure = 'tls';
            $mail->Port = 587;

            $mail->setFrom('cuello.juanpablo@gmail.com', 'Agenda Diaria');
            $mail->addAddress($email);
            $mail->isHTML(false);
            $mail->Subject = "Agendas del día - $today";
            $mail->Body    = $body;

            $mail->send();
            file_put_contents("log.txt", "Correo enviado a $responsable <$email>" . PHP_EOL, FILE_APPEND);
        } catch (Exception $e) {
            file_put_contents("log.txt", "Error al enviar a $responsable <$email>: {$mail->ErrorInfo}" . PHP_EOL, FILE_APPEND);
        }
    } else {
        file_put_contents("log.txt", "Sin agendas para $responsable el $today" . PHP_EOL, FILE_APPEND);
    }
}


$conn->close();
echo "Correos procesados.";
?>

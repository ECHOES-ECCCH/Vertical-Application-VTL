<?php
    function create_user_info($uid, $mail)
    {
        return "{\"uid\":\"$uid\",\"email\":\"$mail\"}";
    }

    function encrypt_value($value)
    {
        $keyfile = realpath(dirname(__FILE__) . '/.ssh/publickey.cer');
        $pubkey = openssl_pkey_get_public(file_get_contents($keyfile));

        $encrypted = '';
        if (!openssl_public_encrypt($value, $encrypted, $pubkey)) {
            die('Failed to encrypt value');
        }

        openssl_free_key($pubkey);
        return base64_encode($encrypted);
    }

    if (isset($_REQUEST["returnUrl"]) && isset($_SERVER["mail"]) && isset($_SERVER["eduPersonUniqueID"])) {
        header('Location: ' . $_REQUEST["returnUrl"] . '?email=' . encrypt_value(create_user_info($_SERVER["eduPersonUniqueID"], $_SERVER["mail"])));
    } else {
        die("Missing required parameters");
    }
    exit;
?>
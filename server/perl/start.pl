#!/usr/bin/env perl

use Getopt::Long;
use Mojo::Webqq;

my $ffm_port    = 5004;
my $openqq_port = 5003;
my $conf_file   = 'perl/conf.json';

GetOptions(
    "ffm-port=i"    => \$ffm_port,
    "openqq-port=i" => \$openqq_port,
    "conf-file=s"   => \$conf_file
) or die("Error in command line arguments\n");

my $client = Mojo::Webqq->new(
    log_encoding           => 'utf8',
    poll_failure_count_max => 20
);

$client->load('UploadQRcode');

$client->load('ShowMsg');

$client->load(
    'FFM',
    data => {
        api_url => 'https://fcm.googleapis.com/fcm/send',
        api_key =>
'AAAABvjXwsM:APA91bF0X8YKcyTJcUdTLB1lc6Xb-03eIHCLy7PKHCwVYCL6XqEB7eS8o3i0amPOPi-R4i_ldlVtnPcYLtf4DwS4qgTi5Ra8Uyl9pGT02iJDE9Ovc-5dUoNSpgWUUZPn0KN2gJjeYLhO',
        conf_file => $conf_file,
        listen    => [
            {
                host => '127.0.0.1',
                port => $ffm_port,
            }
        ]
    }
);

$client->load(
    'Openqq',
    data => {
        listen => [
            {
                host => '127.0.0.1',
                port => $openqq_port,
            }
        ]
    }
);

$client->run();

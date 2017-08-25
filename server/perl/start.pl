#!/usr/bin/env perl

use Getopt::Long;
use Mojo::Webqq;

my $node_port   = 5004;
my $openqq_port = 5003;

GetOptions(
    "node-port=i"   => \$node_port,
    "openqq-port=i" => \$openqq_port,
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
        api_url => 'http://127.0.0.1:' . $node_port . '/ffm/send'
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

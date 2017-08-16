#!/usr/bin/env perl

use Mojo::Webqq;

my $client = Mojo::Webqq->new(
    log_encoding => 'utf8',
    poll_failure_count_max => 20
);

$client->load('UploadQRcode');

$client->load('ShowMsg');

$client->load(
    'FFM',
    data => {
        api_url => 'https://fcm.googleapis.com/fcm/send',
        api_key => 'AAAABvjXwsM:APA91bF0X8YKcyTJcUdTLB1lc6Xb-03eIHCLy7PKHCwVYCL6XqEB7eS8o3i0amPOPi-R4i_ldlVtnPcYLtf4DwS4qgTi5Ra8Uyl9pGT02iJDE9Ovc-5dUoNSpgWUUZPn0KN2gJjeYLhO',
        conf_file => '', # 改成你的配置文件的位置
		listen => [
            {
                host => '127.0.0.1',
                port => 5004,
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
                port => 5003,
            }
        ]
    }
);

$client->run();

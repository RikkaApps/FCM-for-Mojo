package Mojo::Webqq::Plugin::RikkaGCM;
our $AUTHOR   = 'rikka@shizuku.moe';
our $SITE     = '';
our $DESC     = '';
our $PRIORITY = 97;
use List::Util qw(first);

sub call {
    my $client = shift;
    my $data   = shift;
    $client->load("UploadQRcode") if !$client->is_load_plugin('UploadQRcode');
    my $api_url = $data->{api_url};
    my $api_key = $data->{api_key}
      or $client->die( "[" . __PACKAGE__ . "]必须指定 api_key" );
    my $registration_ids = $data->{registration_ids} // [];
    if ( ref $registration_ids ne 'ARRAY' or @{$registration_ids} == 0 ) {
        $client->die( "[" . __PACKAGE__ . "]registration_ids 无效" );
    }
    $client->on(
        receive_message => sub {
            my ( $client, $msg ) = @_;

            my %chat;

            $chat{name}             = $msg->sender->displayname;
            $chat{message}{sender}  = $msg->sender->displayname;
            $chat{message}{content} = $msg->content;

            if ( $msg->is_at ) {
                $chat{message}{isAt} = 1;
            }
            if ( $msg->type eq 'friend_message' ) {
                $chat{type} = 0;
                $chat{id}   = $msg->sender->id;
                $chat{uid}  = $msg->sender->uid;
            }
            elsif ( $msg->type eq 'group_message' ) {
                if ( !$isAt ) {
                    return
                          if ref $data->{ban_group} eq "ARRAY"
                      and @{ $data->{ban_group} }
                      and first {
                        $_ =~ /^\d+$/
                          ? $msg->group->uid eq $_
                          : $msg->group->displayname eq $_
                    }
                    @{ $data->{ban_group} };
                    return
                          if ref $data->{allow_group} eq "ARRAY"
                      and @{ $data->{allow_group} }
                      and !first {
                        $_ =~ /^\d+$/
                          ? $msg->group->uid eq $_
                          : $msg->group->displayname eq $_
                    }
                    @{ $data->{allow_group} };
                }
                $json{type} = 1;
                $chat{id}   = $msg->group->id;
                $chat{uid}  = $msg->group->uid;
            }
            elsif ( $msg->type eq 'discuss_message' ) {
                return
                      if ref $data->{ban_discuss} eq "ARRAY"
                  and @{ $data->{ban_discuss} }
                  and first {
                    $_ =~ /^\d+$/
                      ? $msg->discuss->uid eq $_
                      : $msg->discuss->displayname eq $_
                }
                @{ $data->{ban_discuss} };
                return
                      if ref $data->{allow_discuss} eq "ARRAY"
                  and @{ $data->{allow_discuss} }
                  and !first {
                    $_ =~ /^\d+$/
                      ? $msg->discuss->uid eq $_
                      : $msg->discuss->displayname eq $_
                }
                @{ $data->{allow_discuss} };
                $json{type} = 2;
                $chat{id}   = $msg->discuss->id;
                $chat{uid}  = $msg->discuss->uid;
            }
            elsif ( $msg->type eq 'sess_message' ) {
            }

            $client->http_post(
                $api_url,
                { 'Authorization' => "key=$api_key", json => 1 },
                json => {
                    registration_ids => $registration_ids,
                    priority         => 'high',
                    data             => \%chat,
                },
                sub {
                    my $json = shift;
                    if ( not defined $json ) {
                        $client->debug( "[". __PACKAGE__. "]GCM消息推送失败: 返回结果异常" );
                        return;
                    }
                    else {
                        $client->debug( "[". __PACKAGE__. "]GCM消息推送完成：$json->{multicast_id}/$json->{success}/$json->{failure}"
                        );
                    }
                }
            );
        }
    );

    $client->on(
        all_event => sub {
            my ( $client, $event, @args ) = @_;

            if (    $event ne 'login'
                and $event ne 'input_qrcode'
                and $event ne 'stop' )
            {
                return;
            }

            my %chat;
            $chat{type} = 3;
            $chat{message}{sender} = $event;
            if ( $event eq 'input_qrcode' ) {
                $chat{message}{content} = $client->qrcode_upload_url;
            }
            $client->http_post(
                $api_url,
                {
                    'Authorization'       => "key=$api_key",
                    json                  => 1,
                    blocking              => 1,
                    ua_connect_timeout    => 5,
                    ua_request_timeout    => 5,
                    ua_inactivity_timeout => 5,
                    ua_retry_times        => 1
                },
                json => {
                    registration_ids => $registration_ids,
                    collapse_key     => 'system_event',
                    priority         => 'high',
                    data             => \%chat,
                },
                sub {
                    my $json = shift;
                    if ( not defined $json ) {
                        $client->debug( "[". __PACKAGE__ . "]GCM消息推送失败: 返回结果异常" );
                        return;
                    }
                    else {
                        $client->debug( "[". __PACKAGE__. "]GCM消息推送完成：$json->{multicast_id}/$json->{success}/$json->{failure}"
                        );
                    }
                }
            );
        }
    );
}
1;

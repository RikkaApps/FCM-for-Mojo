package Mojo::Webqq::Plugin::RikkaGCM;
our $AUTHOR   = 'rikka@shizuku.moe';
our $SITE     = '';
our $DESC     = '';
our $PRIORITY = 97;

use Data::Dumper;
use List::Util qw(first);
use Mojo::JSON qw(decode_json encode_json);
use Mojo::Webqq::Server;

my $server;

sub makeIdArray {
    my @registration_ids = @_;
    my @ids;
    foreach my $n (@registration_ids) {
        push( @ids, $n->{id} );
    }
    return \@ids;
}

sub call {
    my $client = shift;
    my $data   = shift;
    $client->load("UploadQRcode") if !$client->is_load_plugin('UploadQRcode');
    my $api_url = $data->{api_url};
    my $api_key = $data->{api_key}
      or $client->die( "[" . __PACKAGE__ . "] 必须指定 api_key" );
    my $conf_file = $data->{conf_file}
      or $client->die( "[" . __PACKAGE__ . "] 必须指定 conf_file" );

    my $json_text = do {
        open( my $json_fh, "<:encoding(UTF-8)", $conf_file )
          or $client->die(
            "[" . __PACKAGE__ . "] 无法打开 conf_file \$filename\": $!\n" );
        local $/;
        <$json_fh>;
    };
    my $conf             = decode_json $json_text;
    my $registration_ids = $conf->{registration_ids};

    my $ids = makeIdArray(@$registration_ids);

    #$client->print(Dumper($ids));
    #$client->print(Dumper($conf));

    $client->on(
        receive_message => sub {
            my ( $client, $msg ) = @_;

            my %chat;

            $chat{message}{sender}    = $msg->sender->displayname;
            $chat{message}{content}   = $msg->content;
            $chat{message}{timestamp} = $msg->time;

            if ( $msg->is_at ) {
                $chat{message}{isAt} = 1;
            }
            if ( $msg->type eq 'friend_message' ) {
                $chat{type} = 0;
                $chat{id}   = $msg->sender->id;
                $chat{uid}  = $msg->sender->uid;
                $chat{name} = $msg->sender->displayname;
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
                $chat{type} = 1;
                $chat{id}   = $msg->group->id;
                $chat{uid}  = $msg->group->uid;
                $chat{name} = $msg->group->displayname;
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
                $chat{type} = 2;
                $chat{id}   = $msg->discuss->id;
                $chat{uid}  = $msg->discuss->uid;
                $chat{name} = $msg->discuss->displayname;
            }
            elsif ( $msg->type eq 'sess_message' ) {
            }

            $client->http_post(
                $api_url,
                { 'Authorization' => "key=$api_key", json => 1 },
                json => {
                    registration_ids => $ids,
                    priority         => 'high',
                    data             => \%chat,
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
            $chat{type}               = 3;
            $chat{message}{sender}    = $event;
            $chat{message}{timestamp} = time();
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
                    registration_ids => $ids,
                    collapse_key     => 'system_event',
                    priority         => 'high',
                    data             => \%chat,
                }
            );
        }
    );

    package Mojo::Webqq::Plugin::RikkaGCM::App::Controller;
    use Data::Dumper;
    use Mojo::JSON ();
    use Mojo::Util ();
    use base qw(Mojolicious::Controller);

    sub render {
        my $self = shift;
        if ( $_[0] eq 'json' ) {
            $self->res->headers->content_type('application/json');
            $self->SUPER::render(
                data => Mojo::JSON::to_json( $_[1] ),
                @_[ 2 .. $#_ ]
            );
        }
        else { $self->SUPER::render(@_) }
    }

    sub safe_render {
        my $self = shift;
        $self->render(@_) if ( defined $self->tx and !$self->tx->is_finished );
    }

    sub param {
        my $self = shift;
        my $data = $self->SUPER::param(@_);
        defined $data ? Mojo::Util::encode( "utf8", $data ) : undef;
    }

    sub params {
        my $self = shift;
        my $hash = $self->req->params->to_hash;
        $client->reform($hash);
        return $hash;
    }

    package Mojo::Webqq::Plugin::RikkaGCM::App;
    use Encode ();
    use Data::Dumper;
    use Mojo::JSON qw(decode_json encode_json);
    use Mojolicious::Lite;
    no utf8;
    app->controller_class('Mojo::Webqq::Plugin::RikkaGCM::App::Controller');
    app->hook(
        after_render => sub {
            my ( $c, $output, $format ) = @_;

            $c->res->headers->header( "Access-Control-Allow-Origin" => "*" );

            my $datatype = $c->param("datatype");
            return if not defined $datatype;
            return if defined $datatype and $datatype ne 'jsonp';
            my $jsoncallback = $c->param("callback") || 'jsoncallback' . time;
            return if not defined $jsoncallback;
            $$output = "$jsoncallback($$output)";
        }
    );

    get '/ffm/get_registration_ids' =>
      sub { $_[0]->safe_render( json => [@$registration_ids] ); };

    post '/ffm/update_registration_ids' => sub {
        my $c = shift;
        my $p = $c->req->json;

        $registration_ids = $p;
        $conf->{registration_ids} = $registration_ids;
        $ids = Mojo::Webqq::Plugin::RikkaGCM::makeIdArray(@$registration_ids);

        #$client->print(Dumper($registration_ids));
        #$client->print(Dumper($conf));
        #$client->print( Dumper($ids) );

        #my $add_ids =$p->{registration_ids};

        #my %hash;
        #@hash{@$add_ids, @$registration_ids}++;
        #my @ids = sort keys %hash;

        #$registration_ids = \@ids;
        #$conf->{registration_ids} = $registration_ids;

        #$client->print(Dumper($conf));
        #$client->print(Dumper($registration_ids));

        open( my $json_fh, ">", $conf_file )
          or $client->die(
            "[" . __PACKAGE__ . "] 无法打开 conf_file \$filename\": $!\n" );

        print $json_fh encode_json $conf ;

        $c->safe_render( json => { code => 0, status => "updated" } );
    };

    package Mojo::Webqq::Plugin::RikkaGCM;
    $server = Mojo::Webqq::Server->new();
    $server->app( $server->build_app("Mojo::Webqq::Plugin::RikkaGCM::App") );
    $server->app->secrets("hello world");
    $server->app->log( $client->log );
    if ( ref $data eq "HASH" and ref $data->{listen} eq "ARRAY" ) {
        my @listen;
        for my $listen ( @{ $data->{listen} } ) {
            if ( $listen->{tls} ) {
                my $listen_url =
                    'https://'
                  . ( $listen->{host} // "0.0.0.0" ) . ":"
                  . ( $listen->{port} // 443 );
                my @ssl_option;
                for ( keys %$listen ) {
                    next if ( $_ eq 'tls' or $_ eq 'host' or $_ eq 'port' );
                    my $key = $_;
                    my $val = $listen->{val};
                    $key =~ s/^tls_//g;
                    push @ssl_option, "$_=$listen->{$_}";
                }
                $listen_url .= "?" . join( "&", @ssl_option ) if @ssl_option;
                push @listen, $listen_url;
            }
            else {
                push @listen,
                    'http://'
                  . ( $listen->{host} // "0.0.0.0" ) . ":"
                  . ( $listen->{port} // 5000 );
            }
        }
        $server->listen( \@listen );
    }
    $server->start;
}
1;

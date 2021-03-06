package Perun::beans::Resource;

use strict;
use warnings;

use Perun::Common;

use overload
'""' => \&toString;

sub toString {
	my $self = shift;

	my $id = $self->{_id};
	my $name = $self->{_name};
	my $description = $self->{_description};

	my $str = 'Resource (';
	$str .= "id: $id, " if ($id);
	$str .= "name: $name, " if ($name);
	$str .= "description: $description" if ($description);
	$str .= ')';

	return $str;
}

sub new
{
	bless({});
}

sub fromHash
{
	return Perun::Common::fromHash(@_);
}

sub TO_JSON
{
	my $self = shift;

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id}*1;
	} else {
		$id = 0;
	}

	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}

	my $description;
	if (defined($self->{_description})) {
		$description = "$self->{_description}";
	} else {
		$description = undef;
	}

	return {id => $id, name => $name, description => $description};
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub setId
{
	my $self = shift;
	$self->{_id} = shift;

	return;
}

sub getName
{
	my $self = shift;

	return $self->{_name};
}

sub setName
{
	my $self = shift;
	$self->{_name} = shift;

	return;
}

sub getDescription
{
	my $self = shift;

	return $self->{_description};
}

sub setDescription
{
	my $self = shift;
	$self->{_description} = shift;

	return;
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->{_id}, $self->{_name});
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Name');
}


1;

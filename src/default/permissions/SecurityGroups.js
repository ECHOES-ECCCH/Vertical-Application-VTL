const SecurityGroupsPermissions = {
	"canCreateTranscription": ["USER", "SUPER_ADMIN"],
	"canAccessProjectsDashboard": ["SUPER_ADMIN"],
	"canChangeProjectLimit": ["SUPER_ADMIN"],
	"canAccessUsersDashboard": ["SUPER_ADMIN"],
	"canTrainOCR": ["SUPER_ADMIN"],
};

export const CheckSecurityGroupPermission = (permission, securityGroup) => {
	return SecurityGroupsPermissions[permission].includes(securityGroup);
};

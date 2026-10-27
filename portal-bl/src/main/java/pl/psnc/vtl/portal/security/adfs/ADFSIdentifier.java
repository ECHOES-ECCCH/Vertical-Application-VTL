package pl.psnc.vtl.portal.security.adfs;

import lombok.Value;

@Value
//Active Directory Federation Service Indentifier
public class ADFSIdentifier {

    public final String uuid;
    public final String email;
}

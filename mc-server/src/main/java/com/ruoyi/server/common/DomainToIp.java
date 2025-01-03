package com.ruoyi.server.common;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/*
 * 域名转IP
 * 作者：Memory
 */
@Slf4j
public class DomainToIp {


    public static String domainToIp(String domain) {
        if (domain.matches("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)")) {
            return domain;
        }
        InetAddress address = null;
        try {
            address = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        log.info("Domain: {}", domain);
        log.info("IP Address: {}", address.getHostAddress());
        return address.getHostAddress();
    }
}

package org.bombusim.lime.data;

public class SelfContact extends Contact {

    public SelfContact(String jid, String name, long id) {
        super(jid, name, id);
    }

    /**
     * Self-contact always can send/receive own presences and messages,
     * so we always returns SUBSCR_BOTH
     */
    @Override
    public int getSubscription() { return SUBSCR_BOTH; }
    
    @Override
    public String getAllGroups() { return null; }
    
    @Override
    public void setUpdate(int upd) {
        if (upd == UPDATE_DROP) return;
        super.setUpdate(upd);
    }
}

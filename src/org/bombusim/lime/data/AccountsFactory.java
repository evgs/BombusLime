package org.bombusim.lime.data;

import java.util.ArrayList;

import org.bombusim.xmpp.XmppAccount;

import android.content.Context;

public class AccountsFactory {
	private AccountsFactory() {}
	
	public static ArrayList<XmppAccount> loadAccounts(Context context) {
		ArrayList<XmppAccount> list = new ArrayList<XmppAccount>();
		
		AccountDbAdapter dba = new AccountDbAdapter(context);
		dba.open();
		long[] indexes = dba.getAccountIndexes();
		
		if (indexes != null)   for (long index : indexes) {
			XmppAccount a=dba.getAccount(index);
			a._id = index;
			list.add(a);
		}
		
		if (list.isEmpty()) {
			list.add(new XmppAccount());
		}
		
		dba.close();
		
		return list;
	}

	public static void saveAccount(Context context, XmppAccount account ) {
		AccountDbAdapter dba = new AccountDbAdapter(context);

		dba.open();
		
		account._id = dba.putAccount(account, account._id);
		
		dba.close();
		
	}
}

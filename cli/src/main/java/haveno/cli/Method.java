/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.cli;

/**
 * Currently supported API methods.
 */
public enum Method {
    canceloffer,
    closetrade,
    confirmpaymentreceived,
    confirmpaymentsent,
    createoffer,
    editoffer,
    createpaymentacct,
    createcryptopaymentacct,
    getaddressbalance,
    getbalance,
    getxmrprice,
    getfundingaddresses,
    @Deprecated // Since 27-Dec-2021.
    getmyoffer, // Endpoint to be removed from future version. Use getoffer instead.
    getmyoffers,
    getoffer,
    getoffers,
    getpaymentacctform,
    getpaymentaccts,
    getpaymentmethods,
    gettrade,
    gettrades,
    failtrade,
    unfailtrade,
    gettransaction,
    getunusedxmraddress,
    getversion,
    lockwallet,
    registerdisputeagent,
    removewalletpassword,
    setwalletpassword,
    takeoffer,
    unlockwallet,
    withdrawfunds,
    stop,
    getxmrseed,
    getxmrprimaryaddress,
    getxmrnewsubaddress,
    getxmrtxs,
    createxmrtx,
    relayxmrtxs,
    getchatmessages,
    sendchatmessage,
    getpaymentaccountform,
    deletepaymentaccount,
    validateformfield,
    completetrade,
    sendxmr,
    // Account management methods
    accountexists,
    isaccountopen,
    createaccount,
    openaccount,
    isappinitialized,
    changepassword,
    closeaccount,
    deleteaccount,
    backupaccount,
    restoreaccount,
    // Dispute methods
    getdispute,
    getdisputes,
    opendispute,
    resolvedispute,
    senddisputechatmessage,
    // XMR connection methods
    addconnection,
    removeconnection,
    getconnection,
    getconnections,
    setconnection,
    checkconnection,
    checkconnections,
    startcheckingconnection,
    stopcheckingconnection,
    getbestconnection,
    setautoswitch,
    getautoswitch,
    // XMR node methods
    isxmrnodeonline,
    getxmrnodesettings,
    startxmrnode,
    stopxmrnode
}

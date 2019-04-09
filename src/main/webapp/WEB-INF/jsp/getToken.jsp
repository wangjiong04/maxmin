<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Get Access Token from TD Ameritrade</title>
</head>
<body>
<h3 style="color: red;">Get Access Token from TD Ameritrade</h3>

<div id="addEmployee">
    <form:form action="redirectPage"
               method="post">

        <table>
            <tr>
                <td>
                    <label>response_type</label>
                </td>
                <td>
                    <input type="text" name="response_type" value="code"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>client_id</label>
                </td>
                <td>
                    <input type="text" name="client_id" value="YAKOBUX999911@AMER.OAUTHAP"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>redirect_uri</label>
                </td>
                <td>
                    <input type="text" name="redirect_uri" value="http://localhost:3001/app/api/connect"/>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="SUBMIT" value="Get Token"/>
                </td>
                <td>

                </td>
            </tr>
        </table>
    </form:form>
</div>
</body>
</html>

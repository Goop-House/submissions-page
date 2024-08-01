// @ts-ignore
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
// @ts-ignore
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
};

declare global {
  namespace Deno {
    export const env: {
      get(key: string): string | undefined;
    };
  }
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    const supabase = createClient(supabaseUrl, supabaseKey);

    const { data: submissions, error } = await supabase
      .from('submissions')
      .select('*');

    if (error) throw error;

    const submissionsWithUrls = await Promise.all(submissions.map(async (submission) => {
      const audioUrl = await supabase.storage
        .from('audio')
        .createSignedUrl(submission.audio_path, 3600); // 1 hour expiry

      let artworkUrl = null;
      if (submission.art_path) {
        artworkUrl = await supabase.storage
          .from('artwork')
          .createSignedUrl(submission.art_path, 3600); // 1 hour expiry
      }

      return {
        ...submission,
        audioUrl: audioUrl.data?.signedUrl,
        artworkUrl: (artworkUrl as { data: { signedUrl: string } } | null)?.data?.signedUrl
      };
    }));

    return new Response(JSON.stringify({ submissions: submissionsWithUrls }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });

  } catch (error) {
    console.error('Error:', error);
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});